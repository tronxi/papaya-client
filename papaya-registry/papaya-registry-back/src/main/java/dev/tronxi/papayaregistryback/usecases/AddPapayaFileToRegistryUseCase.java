package dev.tronxi.papayaregistryback.usecases;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;
import dev.tronxi.papayaregistryback.models.papayafile.PapayaFile;
import dev.tronxi.papayaregistryback.persistence.PapayaFileRegistryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class AddPapayaFileToRegistryUseCase {

    private final PapayaFileRegistryRepository papayaFileRegistryRepository;

    public AddPapayaFileToRegistryUseCase(PapayaFileRegistryRepository papayaFileRegistryRepository) {
        this.papayaFileRegistryRepository = papayaFileRegistryRepository;
    }


    public void add(MultipartFile multipartFile, String description) {
        Optional<PapayaFile> maybePapayaFile = convertMultipartFileToPapayaFile(multipartFile);
        if (maybePapayaFile.isPresent()) {
            PapayaFile papayaFile = maybePapayaFile.get();
            PapayaFileRegistry papayaFileRegistry = new PapayaFileRegistry();
            papayaFileRegistry.setFileId(papayaFile.getFileId());
            papayaFileRegistry.setFileName(papayaFile.getFileName());
            papayaFileRegistry.setPath(papayaFile.getFileId() + ".papaya");
            papayaFileRegistry.setDownloads(0L);
            papayaFileRegistry.setDescription(description);
            papayaFileRegistry.setPapayaFile(papayaFile);
            papayaFileRegistryRepository.save(papayaFileRegistry);
        }
    }

    private Optional<PapayaFile> convertMultipartFileToPapayaFile(MultipartFile multipartFile) {
        try {
            String jsonContent = new String(multipartFile.getBytes());
            ObjectMapper objectMapper = new ObjectMapper();
            return Optional.of(objectMapper.readValue(jsonContent, PapayaFile.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
