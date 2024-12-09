package dev.tronxi.papayaregistryback.usecases;

import dev.tronxi.papayaregistryback.models.PaginatedQuery;
import dev.tronxi.papayaregistryback.persistence.PapayaFileRegistryRepository;
import org.springframework.stereotype.Service;

@Service
public class RetrieveWithQueryUseCase {

    private final PapayaFileRegistryRepository papayaFileRegistryRepository;

    public RetrieveWithQueryUseCase(PapayaFileRegistryRepository papayaFileRegistryRepository) {
        this.papayaFileRegistryRepository = papayaFileRegistryRepository;
    }

    public PaginatedQuery retrieveWithQuery(String query, int pageNumber, int pageSize) {
        return papayaFileRegistryRepository.retrieveWithQuery(query, pageNumber, pageSize);
    }
}
