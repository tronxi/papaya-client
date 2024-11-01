package dev.tronxi.papayaclient.files;

import com.fasterxml.jackson.databind.ObjectMapper;
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

        Path store = calculateStorePath(inputFile.getName(), hash);
        if (!store.toFile().exists()) {
            store.toFile().mkdirs();
        }

        int parts = 100;
        int partSize = (int) Math.ceil((double) bytes.length / parts);

        for (int i = 0; i < bytes.length; i += partSize) {
            int end = Math.min(bytes.length, i + partSize);
            byte[] part = Arrays.copyOfRange(bytes, i, end);
            String partName = String.valueOf(i);
            String partHash = hashGenerator.generateHash(part);
            PartFile partFile = new PartFile(partName, partHash);
            papayaFile.addPartFile(partFile);
            Files.write(store.resolve(partName), part);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(store.resolve(inputFile.getName() + "_" + hash + ".papaya").toFile(), papayaFile);
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

    private Path calculateStorePath(String inputFileName, String hash) {
        String fileNameWithoutExtension = inputFileName.contains(".") ? inputFileName.substring(0, inputFileName.lastIndexOf(".")) : inputFileName;
        return storePath.resolve(fileNameWithoutExtension + "_" + hash);
    }
}
