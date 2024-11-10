package dev.tronxi.papayatracker.usecases;

import dev.tronxi.papayatracker.models.Peer;
import dev.tronxi.papayatracker.persistence.PeerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class RetrievePeersUseCase {

    Logger logger = Logger.getLogger(RetrievePeersUseCase.class.getName());

    private final PeerRepository peerRepository;

    public RetrievePeersUseCase(PeerRepository peerRepository) {
        this.peerRepository = peerRepository;
    }

    public List<Peer> retrievePeers() {
        logger.info("Retrieving peers...");
        return peerRepository.findAll();
    }
}
