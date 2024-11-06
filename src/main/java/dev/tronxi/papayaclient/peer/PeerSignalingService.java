package dev.tronxi.papayaclient.peer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PeerSignalingService {

    Logger logger = Logger.getLogger(PeerSignalingService.class.getName());

    public PeerSignalingService() {
        logger.setLevel(Level.INFO);
    }

    List<Peer> peers = new ArrayList<>();

    {
//        peers.add(new Peer("localhost", 3390));
//        peers.add(new Peer("192.168.1.134", 3390));
        peers.add(new Peer("192.168.1.129", 3390));
    }

    private Peer peer;

    public List<Peer> retrievePeers() {
        return peers.stream()
                .filter(p -> !p.equals(peer))
                .toList();
    }

    public void initialSend(Peer peer) {
        this.peer = peer;
        logger.log(Level.INFO, "Initial peer send: " + peer);
        send(peer);
    }

    @Scheduled(fixedRate = 300000)
    public void refreshSend() {
        logger.log(Level.INFO, "Refresh peer send: " + peer);
        if (peer != null) {
            send(peer);
        } else {
            logger.severe("Peer is null");
        }
    }

    private void send(Peer peer) {
        if (!peers.contains(peer)) {
            peers.add(peer);
            logger.log(Level.INFO, "Peer " + peer + " added to list");
        }
    }
}
