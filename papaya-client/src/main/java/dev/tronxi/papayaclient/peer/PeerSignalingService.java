package dev.tronxi.papayaclient.peer;

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
public class PeerSignalingService {

    Logger logger = Logger.getLogger(PeerSignalingService.class.getName());

    @Value("${papaya.tracker}")
    private String trackerAddress;
    private Peer peer;


    List<Peer> peers = new ArrayList<>();

    public PeerSignalingService() {
        logger.setLevel(Level.INFO);
    }

    public List<Peer> retrievePeers() {
        List<Peer> response = new RestTemplate().exchange(
                trackerAddress + "/peer",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Peer>>() {
                }
        ).getBody();
        logger.info("Peers: " + response);
        if (response != null) {
            return response.stream().filter(p -> !p.equals(peer))
                    .toList();
        }
        return List.of();
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
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Peer> entity = new HttpEntity<>(peer, headers);
        new RestTemplate().exchange(
                trackerAddress + "/peer",
                HttpMethod.POST,
                entity,
                Void.class
        );
    }
}
