package dev.tronxi.papayaclient.peer.services;

import dev.tronxi.papayaclient.peer.Peer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PeerTrackerService {

    Logger logger = Logger.getLogger(PeerTrackerService.class.getName());

    @Value("${papaya.tracker}")
    private String trackerAddress;
    private Peer peer;
    private List<Peer> lastPeers = new ArrayList<>();


    public List<Peer> retrievePeers() {
        List<Peer> currentPeers = retrieveCurrentPeers();
        return currentPeers.stream()
                .filter(p -> !p.equals(peer))
                .toList();
    }

    public List<Peer> retrieveNewPeers() {
        List<Peer> currentPeers = retrievePeers();
        List<Peer> newPeers = new ArrayList<>();
        for (Peer peer : currentPeers) {
            if(!lastPeers.contains(peer)) {
                newPeers.add(peer);
            }
        }
        lastPeers = currentPeers;
        return newPeers;
    }

    private List<Peer> retrieveCurrentPeers() {
        try {
            List<Peer> response = new RestTemplate().exchange(
                    trackerAddress + "/peer",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Peer>>() {
                    }
            ).getBody();
            logger.info("Peers: " + response);
            if (response != null) {
                return response;
            }
            return List.of();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return List.of();
        }
    }

    public void initialSend(Peer peer) {
        this.peer = peer;
        logger.log(Level.INFO, "Initial peer send: " + peer);
        send(peer);
    }

    @Scheduled(fixedRate = 30000)
    public void refreshSend() {
        logger.log(Level.INFO, "Refresh peer send: " + peer);
        if (peer != null) {
            send(peer);
        } else {
            logger.severe("Peer is null");
        }
    }

    private void send(Peer peer) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Peer> entity = new HttpEntity<>(peer, headers);
            new RestTemplate().exchange(
                    trackerAddress + "/peer",
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to send peer", e);
        }
    }

    public void remove() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Peer> entity = new HttpEntity<>(peer, headers);
            new RestTemplate().exchange(
                    trackerAddress + "/peer",
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to remove peer", e);
        }
    }
}
