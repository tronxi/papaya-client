package dev.tronxi.papayaregistryback.usecases;

import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;
import dev.tronxi.papayaregistryback.persistence.PapayaFileRegistryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetrieveTopDownloadsUseCase {

    private final PapayaFileRegistryRepository papayaFileRegistryRepository;

    public RetrieveTopDownloadsUseCase(PapayaFileRegistryRepository papayaFileRegistryRepository) {
        this.papayaFileRegistryRepository = papayaFileRegistryRepository;
    }

    public List<PapayaFileRegistry> retrieve(int pageNumber, int pageSize) {
        return papayaFileRegistryRepository.retrieveTopDownloads(pageNumber, pageSize);
    }
}
