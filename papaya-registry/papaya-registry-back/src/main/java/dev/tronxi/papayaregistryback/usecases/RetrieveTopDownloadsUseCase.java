package dev.tronxi.papayaregistryback.usecases;

import dev.tronxi.papayaregistryback.models.PaginatedQuery;
import dev.tronxi.papayaregistryback.persistence.PapayaFileRegistryRepository;
import org.springframework.stereotype.Service;


@Service
public class RetrieveTopDownloadsUseCase {

    private final PapayaFileRegistryRepository papayaFileRegistryRepository;

    public RetrieveTopDownloadsUseCase(PapayaFileRegistryRepository papayaFileRegistryRepository) {
        this.papayaFileRegistryRepository = papayaFileRegistryRepository;
    }

    public PaginatedQuery retrieve(int pageNumber, int pageSize) {
        return papayaFileRegistryRepository.retrieveTopDownloads(pageNumber, pageSize);
    }
}
