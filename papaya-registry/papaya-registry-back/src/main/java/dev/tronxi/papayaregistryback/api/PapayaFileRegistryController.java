package dev.tronxi.papayaregistryback.api;

import dev.tronxi.papayaregistryback.models.PaginatedQuery;
import dev.tronxi.papayaregistryback.usecases.AddPapayaFileToRegistryUseCase;
import dev.tronxi.papayaregistryback.usecases.DownloadPapayaFilePathUseCase;
import dev.tronxi.papayaregistryback.usecases.RetrieveTopDownloadsUseCase;
import dev.tronxi.papayaregistryback.usecases.RetrieveWithQueryUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@RestController
@RequestMapping("registry")
@CrossOrigin(origins = "*")
public class PapayaFileRegistryController {

    private final AddPapayaFileToRegistryUseCase addPapayaFileToRegistryUseCase;
    private final DownloadPapayaFilePathUseCase downloadPapayaFilePathUseCase;
    private final RetrieveTopDownloadsUseCase retrieveTopDownloadsUseCase;
    private final RetrieveWithQueryUseCase retrieveWithQueryUseCase;

    public PapayaFileRegistryController(AddPapayaFileToRegistryUseCase addPapayaFileToRegistryUseCase, DownloadPapayaFilePathUseCase downloadPapayaFilePathUseCase, RetrieveTopDownloadsUseCase retrieveTopDownloadsUseCase, RetrieveWithQueryUseCase retrieveWithQueryUseCase) {
        this.addPapayaFileToRegistryUseCase = addPapayaFileToRegistryUseCase;
        this.downloadPapayaFilePathUseCase = downloadPapayaFilePathUseCase;
        this.retrieveTopDownloadsUseCase = retrieveTopDownloadsUseCase;
        this.retrieveWithQueryUseCase = retrieveWithQueryUseCase;
    }

    @PostMapping
    ResponseEntity<Void> savePapayaFile(@RequestParam MultipartFile papayaFile, @RequestParam String description) {
        addPapayaFileToRegistryUseCase.add(papayaFile, description);
        return ResponseEntity.ok().build();
    }


    @GetMapping("{fileId}/download")
    public ResponseEntity<StreamingResponseBody> downloadFileStream(@PathVariable String fileId) {
        Optional<Path> maybeFilePath = downloadPapayaFilePathUseCase.retrieve(fileId);

        if (maybeFilePath.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = maybeFilePath.get();
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        StreamingResponseBody stream = outputStream -> {
            try (var inputStream = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                .body(stream);
    }

    @GetMapping("topdownloads")
    public ResponseEntity<PaginatedQuery> topDownloads(@RequestParam int pageNumber, @RequestParam int pageSize) {
        return ResponseEntity.ok(retrieveTopDownloadsUseCase.retrieve(pageNumber, pageSize));
    }

    @GetMapping
    public ResponseEntity<PaginatedQuery> query(@RequestParam String query, @RequestParam int pageNumber, @RequestParam int pageSize) {
        return ResponseEntity.ok(retrieveWithQueryUseCase.retrieveWithQuery(query, pageNumber, pageSize));
    }
}
