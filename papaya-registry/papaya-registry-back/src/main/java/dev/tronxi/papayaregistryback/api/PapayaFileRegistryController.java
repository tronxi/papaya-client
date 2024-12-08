package dev.tronxi.papayaregistryback.api;

import dev.tronxi.papayaregistryback.usecases.AddPapayaFileToRegistryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("registry")
public class PapayaFileRegistryController {

    private final AddPapayaFileToRegistryUseCase addPapayaFileToRegistryUseCase;

    public PapayaFileRegistryController(AddPapayaFileToRegistryUseCase addPapayaFileToRegistryUseCase) {
        this.addPapayaFileToRegistryUseCase = addPapayaFileToRegistryUseCase;
    }

    @PostMapping
    ResponseEntity<Void> savePapayaFile(@RequestParam MultipartFile papayaFile, @RequestParam String description) {
        addPapayaFileToRegistryUseCase.add(papayaFile, description);
        return ResponseEntity.ok().build();
    }
}
