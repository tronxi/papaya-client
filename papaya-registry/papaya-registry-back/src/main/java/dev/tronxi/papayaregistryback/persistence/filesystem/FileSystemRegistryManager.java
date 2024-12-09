package dev.tronxi.papayaregistryback.persistence.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;


@Component
public class FileSystemRegistryManager {

    @Value("${registry.path}")
    private Path registryPath;

    public boolean savePapayaFile(PapayaFileRegistry papayaFileRegistry) {
        Path filePath = registryPath.resolve(papayaFileRegistry.getPath());
        if (filePath.toFile().exists()) return false;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(filePath.toFile(), papayaFileRegistry.getPapayaFile());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public Path getAbsolutePath(PapayaFileRegistry papayaFileRegistry) {
        return registryPath.resolve(papayaFileRegistry.getPath());
    }
}
