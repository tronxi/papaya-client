package dev.tronxi.papayaregistryback.persistence;

import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface PapayaFileRegistryRepository {

    void save(PapayaFileRegistry papayaFileRegistry);
    Optional<PapayaFileRegistry> findByFileId(String fileId);
    Optional<Path> findPathByFileIdForDownload(String fileId);
    List<PapayaFileRegistry> retrieveTopDownloads(int pageNumber, int pageSize);
}
