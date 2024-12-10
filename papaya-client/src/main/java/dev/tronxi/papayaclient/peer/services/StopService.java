package dev.tronxi.papayaclient.peer.services;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

@Service
public class StopService {

    private final Logger logger = Logger.getLogger(StopService.class.getName());

    private final PeerTrackerService peerTrackerService;

    public StopService(PeerTrackerService peerTrackerService) {
        this.peerTrackerService = peerTrackerService;
    }


    public void stop(ServerSocket serverSocket) {
        logger.info("Stop...");
        peerTrackerService.remove();
        try {
            logger.info("Close socket");
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }
}
