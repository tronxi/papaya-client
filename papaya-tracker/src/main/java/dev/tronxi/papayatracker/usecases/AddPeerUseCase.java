package dev.tronxi.papayatracker.usecases;

import dev.tronxi.papayatracker.models.Peer;
import dev.tronxi.papayatracker.persistence.PeerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.logging.Logger;

@Service
public class AddPeerUseCase {

    Logger logger = Logger.getLogger(AddPeerUseCase.class.getSimpleName());

    private final PeerRepository peerRepository;

    public AddPeerUseCase(PeerRepository peerRepository) {
        this.peerRepository = peerRepository;
    }

    public void add(Peer peer) {
        logger.info("Adding peer " + peer);
        Optional<Peer> maybePeer = peerRepository.find(peer);
        maybePeer.ifPresentOrElse(p -> {
            logger.info("Updating peer " + peer);
            peerRepository.remove(p);
            peerRepository.add(peer);
        }, () -> peerRepository.add(peer));
    }
}
