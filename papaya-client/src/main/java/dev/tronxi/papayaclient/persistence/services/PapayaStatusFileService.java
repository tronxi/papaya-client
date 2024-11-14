package dev.tronxi.papayaclient.persistence.services;

import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatus;
import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatusFile;
import dev.tronxi.papayaclient.persistence.repositories.PapayaStatusFileRepository;
import dev.tronxi.papayaclient.persistence.repositories.PartPeerStatusFileRepository;
import dev.tronxi.papayaclient.persistence.repositories.PartStatusFileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PapayaStatusFileService {

    private final PapayaStatusFileRepository papayaStatusFileRepository;
    private final PartStatusFileRepository partStatusFileRepository;
    private final PartPeerStatusFileRepository partPeerStatusFileRepository;

    public PapayaStatusFileService(PapayaStatusFileRepository papayaStatusFileRepository, PartStatusFileRepository partStatusFileRepository, PartPeerStatusFileRepository partPeerStatusFileRepository) {
        this.papayaStatusFileRepository = papayaStatusFileRepository;
        this.partStatusFileRepository = partStatusFileRepository;
        this.partPeerStatusFileRepository = partPeerStatusFileRepository;
    }

    public void save(PapayaStatusFile papayaStatusFile) {
        papayaStatusFile.getPartStatusFiles().forEach(part -> partPeerStatusFileRepository.saveAll(part.getPartPeerStatusFiles()));
        partStatusFileRepository.saveAll(papayaStatusFile.getPartStatusFiles());
        papayaStatusFileRepository.save(papayaStatusFile);
    }

    public Optional<PapayaStatusFile> findById(String fileId) {
        return papayaStatusFileRepository.findById(fileId);
    }

    public List<PapayaStatusFile> findAllIncomplete() {
        return papayaStatusFileRepository.findAll().stream()
                .filter(papayaStatusFile -> papayaStatusFile.getStatus().equals(PapayaStatus.INCOMPLETE))
                .toList();
    }

    public List<PapayaStatusFile> findAll() {
        return papayaStatusFileRepository.findAll();
    }
}
