package dev.tronxi.papayaclient.configuration;

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

@Configuration
public class UpnpConfiguration {

    @Value("${papaya.port}")
    private int port;

    @Bean
    public GatewayDevice gatewayDevice() throws IOException, ParserConfigurationException, SAXException {
        GatewayDiscover gatewayDiscover = new GatewayDiscover();
        gatewayDiscover.discover();
        GatewayDevice gatewayDevice = gatewayDiscover.getValidGateway();
        if (null != gatewayDevice) {
            System.out.println("Found gateway device: " + gatewayDevice.getModelName() + " " + gatewayDevice.getModelDescription());
        } else {
            System.out.println("No valid gateway device found.");
            return null;
        }
        InetAddress localAddress = gatewayDevice.getLocalAddress();
        System.out.println("Using local address: " + localAddress);
        String externalIPAddress = gatewayDevice.getExternalIPAddress();
        System.out.println("External address: " + externalIPAddress);

        PortMappingEntry portMapping = new PortMappingEntry();
        if (gatewayDevice.getSpecificPortMappingEntry(port, "UDP", portMapping)) {
            System.out.println("Port was already mapped");
        } else {
            gatewayDevice.addPortMapping(port, port, localAddress.getHostAddress(), "UDP", "papaya");
        }
        return gatewayDevice;
    }
}
