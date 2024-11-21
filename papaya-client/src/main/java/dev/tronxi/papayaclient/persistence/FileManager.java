package dev.tronxi.papayaclient.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tronxi.papayaclient.persistence.papayafile.PapayaFile;
import dev.tronxi.papayaclient.persistence.papayafile.PartFile;
import dev.tronxi.papayaclient.persistence.papayastatusfile.JoinStatus;
import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatus;
import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatusFile;
import dev.tronxi.papayaclient.persistence.papayastatusfile.PartStatusFile;
import dev.tronxi.papayaclient.persistence.repositories.PartStatusFileRepository;
import dev.tronxi.papayaclient.persistence.services.ConfigService;
import dev.tronxi.papayaclient.persistence.services.PapayaStatusFileService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
public class FileManager {

    private final String workspace;

    private Path storePath;

    private final PartSizeCalculator partSizeCalculator;
    private final HashGenerator hashGenerator;
    private final PapayaStatusFileService papayaStatusFileService;
    private final PartStatusFileRepository partStatusFileRepository;
    private final Map<String, Function<PapayaStatusFile, Void>> updateFunctions = new HashMap<>();
    private Function<PapayaStatusFile, Void> newPapayaStatusFileFunction;
    private final List<Function<PapayaStatusFile, Void>> deletedPapayaStatusFileFunctions = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(FileManager.class.getName());


    public FileManager(ConfigService configService, PartSizeCalculator partSizeCalculator, HashGenerator hashGenerator, PapayaStatusFileService papayaStatusFileService, PartStatusFileRepository partStatusFileRepository) {
        this.partSizeCalculator = partSizeCalculator;
        this.papayaStatusFileService = papayaStatusFileService;
        this.partStatusFileRepository = partStatusFileRepository;
        this.hashGenerator = hashGenerator;
        this.workspace = configService.retrieveWorkspace();
    }

    @PostConstruct
    public void init() {
        storePath = Path.of(workspace + "/store/");
    }

    public Path getStorePath() {
        return storePath;
    }


    public Optional<Path> split(File inputFile) {
        logger.info("Start split");
        Path inputFilePatch = inputFile.toPath();
        PapayaFile papayaFile = new PapayaFile(inputFile.getName());
        PapayaStatusFile papayaStatusFile = new PapayaStatusFile(papayaFile.getFileName(), papayaFile.getFileId());
        Path store = storePath.resolve(papayaFile.getFileId());
        if (!store.toFile().exists()) {
            store.toFile().mkdirs();
        }
        long partSize = partSizeCalculator.calculate(inputFile.length());
        logger.info("PartSize: " + partSize);
        int numPart = 0;

        try (InputStream inputStream = Files.newInputStream(inputFilePatch)) {
            byte[] buffer = new byte[384000000];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                logger.info("Reading bytes: " + bytesRead);
                byte[] bytes = new byte[bytesRead];
                System.arraycopy(buffer, 0, bytes, 0, bytesRead);
                for (int i = 0; i < bytes.length; i += (int) partSize) {
                    logger.info("Reading part: " + i);
                    long end = Math.min(bytes.length, i + partSize);
                    byte[] part = Arrays.copyOfRange(bytes, i, (int) end);
                    String partName = String.valueOf(numPart);
                    String partHash = hashGenerator.generateHash(part);
                    PartFile partFile = new PartFile(partName, partHash);
                    papayaFile.addPartFile(partFile);
                    Files.write(store.resolve(partName), part);
                    PartStatusFile partStatusFile = new PartStatusFile(partFile.getFileName(), partFile.getFileHash(), PapayaStatus.COMPLETE);
                    papayaStatusFile.addPartStatusFile(partStatusFile);
                    numPart++;
                }
            }
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(store.resolve(papayaFile.getFileId() + ".papaya").toFile(), papayaFile);
            papayaStatusFileService.save(papayaStatusFile);
            if (newPapayaStatusFileFunction != null) {
                newPapayaStatusFileFunction.apply(papayaStatusFile);
            }
            logger.info("PapayaStatusFile saved");
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }

        return Optional.of(store);
    }

    public void startJoinStarted() {
        List<PapayaStatusFile> toJoin = papayaStatusFileService.findAllCompleteAndNotJoined();
        toJoin.forEach(papayaStatusFile -> {
            File file = storePath.resolve(papayaStatusFile.getFileId()).resolve(papayaStatusFile.getFileName()).toFile();
            if (file.exists()) {
                file.delete();
            }
            joinStore(papayaStatusFile);
        });
    }

    public Optional<Path> joinStore(PapayaStatusFile papayaStatusFile) {
        logger.info("Start join");
        File storeFile = storePath.resolve(papayaStatusFile.getFileId()).toFile();
        papayaStatusFile.setJoinStatus(JoinStatus.STARTED);
        papayaStatusFileService.save(papayaStatusFile);
        if (updateFunctions.containsKey(papayaStatusFile.getFileId())) {
            updateFunctions.get(papayaStatusFile.getFileId()).apply(papayaStatusFile);
        }
        List<Path> papayaFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storeFile.toPath(), "*.papaya")) {
            stream.forEach(papayaFiles::add);
            if (papayaFiles.size() != 1) {
                logger.severe("Papaya file not found");
                return Optional.empty();
            }
            Path papayaPath = papayaFiles.getFirst();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                PapayaFile papayaFile = objectMapper.readValue(papayaPath.toFile(), PapayaFile.class);
                Path papayaFilePath = storeFile.toPath().resolve(papayaFile.getFileName());
                logger.info("Start writing for join: " + papayaFilePath);

                try (FileOutputStream fileOutputStream = new FileOutputStream(papayaFilePath.toFile())) {
                    for (PartFile partFile : papayaFile.getPartFiles()) {
                        logger.info("Reading for join:  " + partFile.getFileName());
                        Path partPath = storeFile.toPath().resolve(partFile.getFileName());
                        try {
                            byte[] partByte = Files.readAllBytes(partPath);
                            String partHash = hashGenerator.generateHash(partByte);
                            if (!partHash.equals(partFile.getFileHash())) {
                                logger.severe("part hash does not match: " + partFile.getFileName());
                                return Optional.empty();
                            }
                            logger.info("Writing for join:  " + partByte.length);
                            fileOutputStream.write(partByte);
                        } catch (IOException e) {
                            logger.severe(e.getMessage());
                            return Optional.empty();
                        }
                    }
                    papayaStatusFile.setJoinStatus(JoinStatus.COMPLETED);
                    papayaStatusFileService.save(papayaStatusFile);
                    if (updateFunctions.containsKey(papayaStatusFile.getFileId())) {
                        updateFunctions.get(papayaStatusFile.getFileId()).apply(papayaStatusFile);
                    }

                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }
                return Optional.of(papayaFilePath);
            } catch (IOException e) {
                logger.severe(e.getMessage());
                return Optional.empty();
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Path> generateStatus(File storeFile) {
        logger.info("Start generate status");
        List<Path> papayaFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storeFile.toPath(), "*.papaya")) {
            stream.forEach(papayaFiles::add);
            if (papayaFiles.size() != 1) {
                logger.severe("Papaya file not found");
                return Optional.empty();
            }
            Path papayaPath = papayaFiles.getFirst();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                PapayaFile papayaFile = objectMapper.readValue(papayaPath.toFile(), PapayaFile.class);
                PapayaStatusFile papayaStatusFile = new PapayaStatusFile(papayaFile.getFileName(), papayaFile.getFileId());
                for (PartFile partFile : papayaFile.getPartFiles()) {
                    logger.info("Reading for generate status:  " + partFile.getFileName());
                    Path partPath = storeFile.toPath().resolve(partFile.getFileName());
                    if (partPath.toFile().exists()) {
                        try {
                            byte[] partByte = Files.readAllBytes(partPath);
                            String partHash = hashGenerator.generateHash(partByte);
                            PartStatusFile partStatusFile;
                            if (partHash.equals(partFile.getFileHash())) {
                                partStatusFile = new PartStatusFile(partFile.getFileName(), partFile.getFileHash(), PapayaStatus.COMPLETE);
                            } else {
                                partStatusFile = new PartStatusFile(partFile.getFileName(), partFile.getFileHash(), PapayaStatus.INCOMPLETE);
                            }
                            papayaStatusFile.addPartStatusFile(partStatusFile);
                        } catch (IOException e) {
                            PartStatusFile partStatusFile = new PartStatusFile(partFile.getFileName(), partFile.getFileHash(), PapayaStatus.INCOMPLETE);
                            papayaStatusFile.addPartStatusFile(partStatusFile);
                        }
                    } else {
                        PartStatusFile partStatusFile = new PartStatusFile(partFile.getFileName(), partFile.getFileHash(), PapayaStatus.INCOMPLETE);
                        papayaStatusFile.addPartStatusFile(partStatusFile);
                    }
                }
                if (updateFunctions.containsKey(papayaStatusFile.getFileId())) {
                    updateFunctions.get(papayaStatusFile.getFileId()).apply(papayaStatusFile);
                } else {
                    if (newPapayaStatusFileFunction != null) {
                        newPapayaStatusFileFunction.apply(papayaStatusFile);
                    }
                }
                Path papayaStatusFilePath = storeFile.toPath().resolve(papayaFile.getFileId() + ".papayastatus");
                papayaStatusFileService.save(papayaStatusFile);
                return Optional.of(papayaStatusFilePath);
            } catch (IOException e) {
                logger.severe(e.getMessage());
                return Optional.empty();
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<PapayaFile> retrievePapayaFileFromFileId(String fileId) {
        File papayaFile = storePath.resolve(fileId).resolve(fileId + ".papaya").toFile();
        return retrievePapayaFileFromFile(papayaFile);
    }

    public Optional<PapayaFile> retrievePapayaFileFromFile(File papayaFile) {
        logger.info("Start retrieve papaya file from file");
        if (papayaFile.exists()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                PapayaFile pf = objectMapper.readValue(papayaFile, PapayaFile.class);
                return Optional.of(pf);
            } catch (IOException e) {
                logger.severe(e.getMessage());
                return Optional.empty();
            }
        } else {
            logger.severe("Papaya file not found");
            return Optional.empty();
        }
    }

    public Optional<PapayaStatusFile> retrievePapayaStatusFileFromFile(String fileId) {
        return papayaStatusFileService.findById(fileId);
    }

    public void savePapayaStatusFile(PapayaStatusFile papayaStatusFile) {
        logger.info("Start save papaya status file");
        papayaStatusFileService.save(papayaStatusFile);
        if (updateFunctions.containsKey(papayaStatusFile.getFileId())) {
            updateFunctions.get(papayaStatusFile.getFileId()).apply(papayaStatusFile);
        }
    }

    public void savePartStatusFile(PapayaStatusFile papayaStatusFile, PartStatusFile partStatusFile) {
        partStatusFileRepository.save(partStatusFile);
        if (updateFunctions.containsKey(papayaStatusFile.getFileId())) {
            updateFunctions.get(papayaStatusFile.getFileId()).apply(papayaStatusFile);
        } else {
            if (newPapayaStatusFileFunction != null) {
                newPapayaStatusFileFunction.apply(papayaStatusFile);
            }
        }
    }

    public void writePart(String fileId, String partFileName, ByteArrayOutputStream content) {
        Path file = storePath.resolve(fileId);
        Path partFile = file.resolve(partFileName);
        if (!file.toFile().exists()) {
            file.toFile().mkdirs();
        }
        try {
            logger.info("Writing part: " + fileId + " / " + partFileName);
            Files.write(partFile, content.toByteArray());
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }

    }

    public void createStoreFromPapayaFile(PapayaFile papayaFile) {
        logger.info("Creating store from papaya file: " + papayaFile.getFileName());
        File store = storePath.resolve(papayaFile.getFileId()).toFile();
        if (!store.exists()) {
            store.mkdirs();
            logger.info("Created store from papaya file: " + papayaFile.getFileName());
            Path papayaFilePath = store.toPath().resolve(papayaFile.getFileId() + ".papaya");
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                logger.info("Creating papaya file");
                objectMapper.writeValue(papayaFilePath.toFile(), papayaFile);
                generateStatus(store);
            } catch (IOException e) {
                logger.severe(e.getMessage());
            }
        } else {
            logger.info("Papaya store already exists");
        }
    }

    public List<String> getCompletedParts(String fileId) {
        Optional<PapayaStatusFile> maybePapayaStatusFile = papayaStatusFileService.findById(fileId);
        if (maybePapayaStatusFile.isEmpty()) {
            return Collections.emptyList();
        }
        PapayaStatusFile papayaStatusFile = maybePapayaStatusFile.get();
        return papayaStatusFile.getPartStatusFiles()
                .stream()
                .filter(partStatusFile -> partStatusFile.getStatus() == PapayaStatus.COMPLETE)
                .map(PartStatusFile::getFileName)
                .toList();
    }

    public void setNewPapayaStatusFileFunction(Function<PapayaStatusFile, Void> function) {
        newPapayaStatusFileFunction = function;
    }

    public void addUpdateFunction(String fileId, Function<PapayaStatusFile, Void> updateFunction) {
        updateFunctions.put(fileId, updateFunction);
    }

    public void addDeletedPapayaStatusFileFunction(Function<PapayaStatusFile, Void> deletedFunction) {
        deletedPapayaStatusFileFunctions.add(deletedFunction);
    }

    public List<PapayaStatusFile> findAll() {
        return papayaStatusFileService.findAll();
    }

    public File getPapayaFolder(PapayaStatusFile papayaStatusFile) {
        return storePath.resolve(papayaStatusFile.getFileId()).toFile();
    }

    public void removePapayaFolder(PapayaStatusFile papayaStatusFile) {
        CompletableFuture.runAsync(() -> {
            File papayaFolder = getPapayaFolder(papayaStatusFile);
            if (papayaFolder != null && papayaFolder.exists() && papayaFolder.isDirectory()) {
                try {
                    papayaStatusFileService.remove(papayaStatusFile);
                    deleteDirectory(papayaFolder.toPath());
                } catch (Exception e) {
                    logger.severe(e.getMessage());
                }
            }
        }).thenRun(() -> {
            updateFunctions.remove(papayaStatusFile.getFileId());
            deletedPapayaStatusFileFunctions.forEach(f -> f.apply(papayaStatusFile));
        });
    }

    private void deleteDirectory(Path path) throws IOException {
        try (Stream<Path> paths = Files.walk(path)) {
            paths.sorted((path1, path2) -> path2.compareTo(path1))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            logger.severe(e.getMessage());
                        }
                    });
        }
    }
}
