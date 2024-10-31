package dev.tronxi.papayaclient.udp;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.bitlet.weupnp.GatewayDevice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

@Service
public class UdpClient {

    @Value("${papaya.port}")
    private int port;

    private DatagramSocket socket;

    private final GatewayDevice gatewayDevice;

    public UdpClient(GatewayDevice gatewayDevice) {
        this.gatewayDevice = gatewayDevice;
    }

    public void start(TextArea textArea) {
        try {
            socket = new DatagramSocket(3390);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    while (true) {
                        try {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];
                            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                            socket.receive(datagramPacket);
                            int length = datagramPacket.getLength();
                            outputStream.write(datagramPacket.getData(), 0, length);
                            Platform.runLater(() -> {
                                textArea.setText(textArea.getText() + "\n" + outputStream);
                            });
                        } catch (IOException e) {
                            return null;
                        }
                    }
                }
            };
            new Thread(task).start();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        socket.close();
        try {
            gatewayDevice.deletePortMapping(port,"UDP");
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

}
