package dev.tronxi.papayaregistryback.persistence;

import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;

public interface PapayaFileRegistryRepository {

    void save(PapayaFileRegistry papayaFileRegistry);
}
