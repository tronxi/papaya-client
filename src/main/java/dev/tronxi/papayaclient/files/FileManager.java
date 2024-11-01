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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class FileManager {

    @Value("${papaya.workspace}")
    private String workspace;

    private Path storePath;

    private final HashGenerator hashGenerator;

    public FileManager(HashGenerator hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    @PostConstruct
    public void init() {
        storePath = Path.of(workspace + "/store/");
    }


    public Path split(File inputFile) throws IOException {
        Path inputFilePatch = inputFile.toPath();
        byte[] bytes = Files.readAllBytes(inputFilePatch);
        String hash = hashGenerator.generateHash(bytes);

        PapayaFile papayaFile = new PapayaFile(inputFile.getName(), hash);

        Path store = storePath.resolve(hash);
        if (!store.toFile().exists()) {
            store.toFile().mkdirs();
        }

        int partSize = 250;
        int numPart = 0;
        for (int i = 0; i < bytes.length; i += partSize) {
            int end = Math.min(bytes.length, i + partSize);
            byte[] part = Arrays.copyOfRange(bytes, i, end);
            String partName = String.valueOf(numPart);
            String partHash = hashGenerator.generateHash(part);
            PartFile partFile = new PartFile(partName, partHash);
            papayaFile.addPartFile(partFile);
            Files.write(store.resolve(partName), part);
            numPart++;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(store.resolve(hash + ".papaya").toFile(), papayaFile);
        return store;
    }

    public Optional<Path> joinStore(File storeFile) {
        List<Path> papayaFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storeFile.toPath(), "*.papaya")) {
            stream.forEach(papayaFiles::add);
            if (papayaFiles.size() != 1) {
                return Optional.empty();
            }
            Path papayaPath = papayaFiles.getFirst();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                PapayaFile papayaFile = objectMapper.readValue(papayaPath.toFile(), PapayaFile.class);
                List<byte[]> partsData = new ArrayList<>();
                for (PartFile partFile : papayaFile.getPartFiles()) {
                    Path partPath = storeFile.toPath().resolve(partFile.getFileName());
                    try {
                        byte[] partByte = Files.readAllBytes(partPath);
                        String partHash = hashGenerator.generateHash(partByte);
                        if (!partHash.equals(partFile.getFileHash())) {
                            return Optional.empty();
                        }
                        partsData.add(Files.readAllBytes(partPath));
                    } catch (IOException e) {
                        return Optional.empty();
                    }
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                for (byte[] partData : partsData) {
                    outputStream.write(partData);
                }

                byte[] combinedBytes = outputStream.toByteArray();
                String combinedHash = hashGenerator.generateHash(combinedBytes);
                if (!combinedHash.equals(papayaFile.getFileHash())) {
                    return Optional.empty();
                }
                Path papayaFilePath = storeFile.toPath().resolve(papayaFile.getFileName());
                Files.write(papayaFilePath, combinedBytes);
                return Optional.of(papayaFilePath);
            } catch (IOException e) {
                return Optional.empty();
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Optional<Path> generateStatus(File storeFile) {
        List<Path> papayaFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storeFile.toPath(), "*.papaya")) {
            stream.forEach(papayaFiles::add);
            if (papayaFiles.size() != 1) {
                return Optional.empty();
            }
            Path papayaPath = papayaFiles.getFirst();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                PapayaFile papayaFile = objectMapper.readValue(papayaPath.toFile(), PapayaFile.class);
                PapayaStatusFile papayaStatusFile = new PapayaStatusFile(papayaFile.getFileName(), papayaFile.getFileHash());
                for (PartFile partFile : papayaFile.getPartFiles()) {
                    Path partPath = storeFile.toPath().resolve(partFile.getFileName());
                    if (partPath.toFile().exists()) {
                        try {
                            byte[] partByte = Files.readAllBytes(partPath);
                            String partHash = hashGenerator.generateHash(partByte);
                            System.out.println(partFile.getFileName() + " " + partByte.length);
                            System.out.println("hash length: " + partHash.getBytes().length);
                            System.out.println("name length: " + partFile.getFileName().getBytes().length);
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

                Path papayaStatusFilePath = storeFile.toPath().resolve(papayaFile.getFileHash() + ".papayastatus");
                objectMapper.writeValue(papayaStatusFilePath.toFile(), papayaStatusFile);
                return Optional.of(papayaStatusFilePath);
            } catch (IOException e) {
                return Optional.empty();
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
