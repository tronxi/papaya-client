package dev.tronxi.papayaregistryback.api;

import dev.tronxi.papayaregistryback.models.PapayaFileRegistry;
import dev.tronxi.papayaregistryback.usecases.AddPapayaFileToRegistryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("registry")
public class PapayaFileRegistryController {

    private final AddPapayaFileToRegistryUseCase addPapayaFileToRegistryUseCase;

    public PapayaFileRegistryController(AddPapayaFileToRegistryUseCase addPapayaFileToRegistryUseCase) {
        this.addPapayaFileToRegistryUseCase = addPapayaFileToRegistryUseCase;
    }

    @GetMapping
    ResponseEntity<Void> getAllPeers() {
        PapayaFileRegistry papayaFileRegistry = new PapayaFileRegistry();
        papayaFileRegistry.setFileId("aa");
        papayaFileRegistry.setFileName("name");
        papayaFileRegistry.setPath(Path.of("registry/files/aa"));
        papayaFileRegistry.setDescription("description");
        papayaFileRegistry.setDownloads(0L);
        addPapayaFileToRegistryUseCase.add(papayaFileRegistry);
        return ResponseEntity.ok().build();
    }
}
