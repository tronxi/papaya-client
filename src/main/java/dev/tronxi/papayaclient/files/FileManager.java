package dev.tronxi.papayaclient.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tronxi.papayaclient.files.papayafile.PapayaFile;
import dev.tronxi.papayaclient.files.papayafile.PartFile;
import dev.tronxi.papayaclient.files.papayastatusfile.PapayaStatus;
import dev.tronxi.papayaclient.files.papayastatusfile.PapayaStatusFile;
import dev.tronxi.papayaclient.files.papayastatusfile.PartStatusFile;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class FileManager {

    @Value("${papaya.workspace}")
    private String workspace;

    private Path storePath;

    private final HashGenerator hashGenerator;

    private static final Logger logger = Logger.getLogger(FileManager.class.getName());


    public FileManager(HashGenerator hashGenerator) {
        logger.setLevel(Level.INFO);
        this.hashGenerator = hashGenerator;
    }

    @PostConstruct
    public void init() {
        storePath = Path.of(workspace + "/store/");
    }


    public Optional<Path> split(File inputFile) {
        logger.info("Start split");
        Path inputFilePatch = inputFile.toPath();
        PapayaFile papayaFile = new PapayaFile(inputFile.getName());
        Path store = storePath.resolve(papayaFile.getFileId());
        if (!store.toFile().exists()) {
            store.toFile().mkdirs();
        }

        int partSize = 1000000000;
        int numPart = 0;

        try (InputStream inputStream = Files.newInputStream(inputFilePatch)) {
            byte[] buffer = new byte[2000000000];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                logger.info("Reading bytes: " + bytesRead);
                byte[] bytes = new byte[bytesRead];
                System.arraycopy(buffer, 0, bytes, 0, bytesRead);
                for (int i = 0; i < bytes.length; i += partSize) {
                    logger.info("Reading part: " + i);
                    int end = Math.min(bytes.length, i + partSize);
                    byte[] part = Arrays.copyOfRange(bytes, i, end);
                    String partName = String.valueOf(numPart);
                    String partHash = hashGenerator.generateHash(part);
                    PartFile partFile = new PartFile(partName, partHash);
                    papayaFile.addPartFile(partFile);
                    Files.write(store.resolve(partName), part);
                    numPart++;
                }
            }
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(store.resolve(papayaFile.getFileId() + ".papaya").toFile(), papayaFile);
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }

        return Optional.of(store);
    }

    public Optional<Path> joinStore(File storeFile) {
        logger.info("Start join");
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

                Path papayaStatusFilePath = storeFile.toPath().resolve(papayaFile.getFileId() + ".papayastatus");
                objectMapper.writeValue(papayaStatusFilePath.toFile(), papayaStatusFile);
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

    public Optional<PapayaFile> retrievePapayaFileFromStore(File storeFile) {
        logger.info("Start retrieve papaya file from store");
        List<Path> papayaFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storeFile.toPath(), "*.papaya")) {
            stream.forEach(papayaFiles::add);
            if (papayaFiles.size() != 1) {
                logger.severe("Papaya file not found");
                return Optional.empty();
            }
            Path papayaPath = papayaFiles.getFirst();
            if (papayaPath.toFile().exists()) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    PapayaFile papayaFile = objectMapper.readValue(papayaPath.toFile(), PapayaFile.class);
                    return Optional.of(papayaFile);
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                    return Optional.empty();
                }
            } else {
                logger.severe("Papaya file not found");
                return Optional.empty();
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<PapayaFile> retrievePapayaFileFromFile(File papayaFile) {
        logger.info("Start retrieve papaya file from file");
        if(papayaFile.exists()) {
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

    public void writePart(String fileId, String partFileName, ByteArrayOutputStream content) {
        Path output = Path.of(workspace).resolve("output");
        Path file = output.resolve(fileId);
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
        if(!store.exists()) {
            store.mkdirs();
            logger.info("Created store from papaya file: " + papayaFile.getFileName());
            Path papayaFilePath = store.toPath().resolve(papayaFile.getFileId() + ".papaya");
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                logger.info("Creating papaya file");
                objectMapper.writeValue(papayaFilePath.toFile(), papayaFile);
            } catch (IOException e) {
                logger.severe(e.getMessage());
            }
        } else {
            logger.severe("Papaya store already exists");
        }
    }
}
