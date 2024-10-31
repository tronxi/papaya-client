package dev.tronxi.papayaclient.files;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class FileManager {

    @Value("${papaya.workspace}")
    private String workspace;

    private Path inputPath;
    private Path outputPath;
    private Path storePath;

    @PostConstruct
    public void init() {
        inputPath = Path.of(workspace + "/inputs/");
        outputPath = Path.of(workspace + "/outputs/");
        storePath = Path.of(workspace + "/store/");
    }


    public void split(String inputFileName) throws IOException {
        Path store = calculateStorePath(inputFileName);

        if (!store.toFile().exists()) {
            store.toFile().mkdirs();
        }

        Path inputFilePatch = inputPath.resolve(inputFileName);

        Path info = store.resolve("info.txt");
        Files.writeString(info, inputFilePatch.getFileName().toString() + System.lineSeparator());
        byte[] bytes = Files.readAllBytes(inputFilePatch);
        int parts = 100;
        int partSize = (int) Math.ceil((double) bytes.length / parts);

        for (int i = 0; i < bytes.length; i += partSize) {
            int end = Math.min(bytes.length, i + partSize);
            byte[] part = Arrays.copyOfRange(bytes, i, end);
            Files.write(store.resolve(String.valueOf(i)), part);
            Files.writeString(info, i + System.lineSeparator(), StandardOpenOption.APPEND);
        }
    }

    public void join(String storeName) throws IOException {
        Path store = storePath.resolve(storeName);
        Path info = store.resolve("info.txt");
        String fileName = Files.readAllLines(info).stream()
                .findFirst()
                .orElse("defaultName");

        List<Path> partFiles = Files.list(store)
                .filter(f -> !f.getFileName().toString().equals("info.txt"))
                .sorted(Comparator.comparingInt(p -> Integer.parseInt(p.getFileName().toString())))
                .toList();

        List<byte[]> partsData = new ArrayList<>();
        for (Path partFile : partFiles) {
            partsData.add(Files.readAllBytes(partFile));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (byte[] partData : partsData) {
            outputStream.write(partData);
        }

        byte[] combinedBytes = outputStream.toByteArray();
        if (!outputPath.toFile().exists()) {
            outputPath.toFile().mkdirs();
        }
        Files.write(outputPath.resolve(fileName), combinedBytes);
    }

    private Path calculateStorePath(String inputFileName) {
        String fileNameWithoutExtension = inputFileName.contains(".") ? inputFileName.substring(0, inputFileName.lastIndexOf(".")) : inputFileName;
        return storePath.resolve(fileNameWithoutExtension);
    }
}
