package dev.tronxi.papayaregistryback.persistence;

import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;

import java.util.Optional;

public interface PapayaFileRegistryRepository {

    void save(PapayaFileRegistry papayaFileRegistry);
    Optional<PapayaFileRegistry> findByFileId(String fileId);
}
