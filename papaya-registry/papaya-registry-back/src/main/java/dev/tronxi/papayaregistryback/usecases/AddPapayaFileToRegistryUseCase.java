package dev.tronxi.papayaregistryback.usecases;

import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;
import dev.tronxi.papayaregistryback.persistence.PapayaFileRegistryRepository;
import org.springframework.stereotype.Service;

@Service
public class AddPapayaFileToRegistryUseCase {

    private final PapayaFileRegistryRepository papayaFileRegistryRepository;

    public AddPapayaFileToRegistryUseCase(PapayaFileRegistryRepository papayaFileRegistryRepository) {
        this.papayaFileRegistryRepository = papayaFileRegistryRepository;
    }


    public void add(PapayaFileRegistry papayaRegistry) {
        papayaFileRegistryRepository.save(papayaRegistry);

    }
}
