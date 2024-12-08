package dev.tronxi.papayaregistryback.usecases;

import dev.tronxi.papayaregistryback.persistence.PapayaFileRegistryRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Optional;

@Service
public class RetrievePapayaFilePathUseCase {

    private final PapayaFileRegistryRepository papayaFileRegistryRepository;

    public RetrievePapayaFilePathUseCase(PapayaFileRegistryRepository papayaFileRegistryRepository) {
        this.papayaFileRegistryRepository = papayaFileRegistryRepository;
    }

    public Optional<Path> retrieve(String fileId) {
        return papayaFileRegistryRepository.findPathByFileId(fileId);
    }
}
