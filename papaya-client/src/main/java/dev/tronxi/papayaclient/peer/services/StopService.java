package dev.tronxi.papayaclient.peer.services;

import org.bitlet.weupnp.GatewayDevice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

@Service
public class StopService {

    private final Logger logger = Logger.getLogger(StopService.class.getName());

    @Value("${papaya.port}")
    private int port;

    private final GatewayDevice gatewayDevice;
    private final PeerTrackerService peerTrackerService;

    public StopService(GatewayDevice gatewayDevice, PeerTrackerService peerTrackerService) {
        this.gatewayDevice = gatewayDevice;
        this.peerTrackerService = peerTrackerService;
    }


    public void stop(ServerSocket serverSocket) {
        logger.info("Stop...");
        peerTrackerService.remove();
        try {
            logger.info("Delete port mapping");
            if (gatewayDevice.getLocalAddress() != null) {
                gatewayDevice.deletePortMapping(port, "TCP");
            }
        } catch (IOException | SAXException e) {
            logger.severe(e.getMessage());
        }
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
