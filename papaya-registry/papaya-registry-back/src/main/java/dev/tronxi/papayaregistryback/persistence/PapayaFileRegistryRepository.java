package dev.tronxi.papayaregistryback.persistence;

import dev.tronxi.papayaregistryback.models.PaginatedQuery;
import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;

import java.nio.file.Path;
import java.util.Optional;

public interface PapayaFileRegistryRepository {

    void save(PapayaFileRegistry papayaFileRegistry);

    Optional<PapayaFileRegistry> findByFileId(String fileId);

    Optional<Path> findPathByFileIdForDownload(String fileId);

    PaginatedQuery retrieveTopDownloads(int pageNumber, int pageSize);

    PaginatedQuery retrieveWithQuery(String query, int pageNumber, int pageSize);
}
