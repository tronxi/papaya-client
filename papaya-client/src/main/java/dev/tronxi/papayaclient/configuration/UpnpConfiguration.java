package dev.tronxi.papayaclient.configuration;

import dev.tronxi.papayaclient.peer.Peer;
import dev.tronxi.papayaclient.peer.services.PeerTrackerService;
import dev.tronxi.papayaclient.persistence.services.ConfigService;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.logging.Logger;

@Configuration
public class UpnpConfiguration {

    private static final Logger logger = Logger.getLogger(UpnpConfiguration.class.getName());

    @Value("${papaya.port}")
    private int port;

    private final PeerTrackerService peerTrackerService;
    private final ConfigService configService;

    public UpnpConfiguration(PeerTrackerService peerTrackerService, ConfigService configService) {
        this.peerTrackerService = peerTrackerService;
        this.configService = configService;
    }

    @Bean
    public GatewayDevice gatewayDevice() throws IOException, ParserConfigurationException, SAXException {
        if (configService.retrieveUseOnlyLocalAddress()) {
            try (Socket socket = new Socket()) {
                logger.info("Using local address");
                socket.connect(new InetSocketAddress("google.com", 80));
                String privateIp = socket.getLocalAddress().getHostAddress();
                Peer peer = new Peer(privateIp, port);
                peerTrackerService.initialSend(peer);
            }
        } else {
            logger.info("Using public address");
            URL checkIpUrl = URI.create("https://checkip.amazonaws.com").toURL();
            String publicIp = new BufferedReader(new InputStreamReader(checkIpUrl.openStream())).readLine();
            Peer peer = new Peer(publicIp, port);
            peerTrackerService.initialSend(peer);
        }
        GatewayDiscover gatewayDiscover = new GatewayDiscover();
        gatewayDiscover.discover();
        GatewayDevice gatewayDevice = gatewayDiscover.getValidGateway();
        if (null != gatewayDevice) {
            logger.info("Found gateway device: " + gatewayDevice.getModelName() + " " + gatewayDevice.getModelDescription());
            InetAddress localAddress = gatewayDevice.getLocalAddress();

            PortMappingEntry portMapping = new PortMappingEntry();
            if (gatewayDevice.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
                logger.info("Port was already mapped");
                gatewayDevice.deletePortMapping(port, "TCP");
            }
            gatewayDevice.addPortMapping(port, port, localAddress.getHostAddress(), "TCP", "papaya");
            return gatewayDevice;
        } else {
            logger.severe("Could not find gateway device");
            return new GatewayDevice();
        }
    }

}
