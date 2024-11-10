package dev.tronxi.papayaclient.configuration;

import dev.tronxi.papayaclient.peer.Peer;
import dev.tronxi.papayaclient.peer.PeerSignalingService;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;

@Configuration
public class UpnpConfiguration {

    private static final Logger logger = Logger.getLogger(UpnpConfiguration.class.getName());

    @Value("${papaya.port}")
    private int port;

    private final PeerSignalingService peerSignalingService;

    public UpnpConfiguration(PeerSignalingService peerSignalingService) {
        this.peerSignalingService = peerSignalingService;
    }


    @Bean
    public GatewayDevice gatewayDevice() throws IOException, ParserConfigurationException, SAXException {
        GatewayDiscover gatewayDiscover = new GatewayDiscover();
        gatewayDiscover.discover();
        GatewayDevice gatewayDevice = gatewayDiscover.getValidGateway();
        if (null != gatewayDevice) {
            logger.info("Found gateway device: " + gatewayDevice.getModelName() + " " + gatewayDevice.getModelDescription());
            InetAddress localAddress = gatewayDevice.getLocalAddress();
            logger.info("Found local address: " + localAddress);
            String externalIPAddress = gatewayDevice.getExternalIPAddress();
            logger.info("Found external IP address: " + externalIPAddress);

//            Peer peer = new Peer(gatewayDevice.getExternalIPAddress(), port);
            Peer peer = new Peer(gatewayDevice.getLocalAddress().getHostAddress(), port);
            peerSignalingService.initialSend(peer);
            peerSignalingService.retrievePeers();

            PortMappingEntry portMapping = new PortMappingEntry();
            if (gatewayDevice.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
                logger.info("Port was already mapped");
            } else {
                gatewayDevice.addPortMapping(port, port, localAddress.getHostAddress(), "TCP", "papaya");
            }
            return gatewayDevice;
        } else {
            logger.severe("Could not find gateway device");
            return new GatewayDevice();
        }
    }
}
