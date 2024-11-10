package dev.tronxi.papayatracker.usecases;

import dev.tronxi.papayatracker.models.Peer;
import dev.tronxi.papayatracker.persistence.PeerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class RemovePeerUseCase {

    Logger logger = Logger.getLogger(RemovePeerUseCase.class.getSimpleName());

    private final PeerRepository peerRepository;

    public RemovePeerUseCase(PeerRepository peerRepository) {
        this.peerRepository = peerRepository;
    }

    public void remove(Peer peer) {
        logger.info("Remove " + peer);
        this.peerRepository.remove(peer);
    }

    @Scheduled(fixedRate = 60000)
    public void removeOld() {
        logger.info("Remove old peers");
        long fiveMinutesAgo = System.currentTimeMillis() - 60000;
        List<Peer> peersToRemove =  peerRepository.findAll().stream()
                .filter(peer -> peer.millis() < fiveMinutesAgo)
                .toList();
        peersToRemove.forEach(this::remove);
    }
}
