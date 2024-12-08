package dev.tronxi.papayaregistryback.usecases;

import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;
import dev.tronxi.papayaregistryback.persistence.PapayaFileRegistryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetrieveWithQueryUseCase {

    private final PapayaFileRegistryRepository papayaFileRegistryRepository;

    public RetrieveWithQueryUseCase(PapayaFileRegistryRepository papayaFileRegistryRepository) {
        this.papayaFileRegistryRepository = papayaFileRegistryRepository;
    }

    public List<PapayaFileRegistry> retrieveWithQuery(String query) {
        return papayaFileRegistryRepository.retrieveWithQuery(query);
    }
}
