package dev.tronxi.papayaclient.peer;

import dev.tronxi.papayaclient.files.FileManager;
import dev.tronxi.papayaclient.files.papayafile.PapayaFile;
import jakarta.annotation.PostConstruct;
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
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class PeerConnectionManagerUDP implements PeerConnectionManager {

    private final FileManager fileManager;
    @Value("${papaya.port}")
    private int port;

    @Value("${papaya.workspace}")
    private String workspace;
    private Path storePath;


    private DatagramSocket socket;

    private final GatewayDevice gatewayDevice;

    public PeerConnectionManagerUDP(GatewayDevice gatewayDevice, FileManager fileManager) {
        this.gatewayDevice = gatewayDevice;
        this.fileManager = fileManager;
    }

    @PostConstruct
    public void init() {
        storePath = Path.of(workspace + "/store/");
    }

    @Override
    public void start(TextArea textArea) {
        try {
            socket = new DatagramSocket(port);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    while (true) {
                        try {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            byte[] buffer = new byte[80000];
                            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                            socket.receive(datagramPacket);
                            int length = datagramPacket.getLength();
                            outputStream.write(datagramPacket.getData(), 0, length);
                            if (length > 0) {
                                byte[] receivedData = outputStream.toByteArray();
                                int typeByte = receivedData[0];
                                String message = "";
                                PeerMessageType peerMessageType = PeerMessageType.fromValue(typeByte);
                                switch (peerMessageType) {
                                    case PART_FILE -> {
                                        message = receivePartFile(datagramPacket, receivedData);
                                    }
                                    case INVALID -> {
                                        message = "Invalid";
                                    }
                                }
                                String finalMessage = message;
                                Platform.runLater(() -> {
                                    textArea.setText(finalMessage);
                                });
                            }
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

    private String receivePartFile(DatagramPacket datagramPacket, byte[] receivedData) {
        String message;
        ByteArrayOutputStream fileHash = new ByteArrayOutputStream();
        try {
            fileHash.write(Arrays.copyOfRange(receivedData, 1, 33));
            ByteArrayOutputStream partFileName = new ByteArrayOutputStream();
            int i = 33;
            char charAtIndex;
            do {
                charAtIndex = (char) receivedData[i];
                if (charAtIndex != '#') {
                    partFileName.write(receivedData[i]);
                }
                i++;
            } while (charAtIndex != '#');

            byte[] dataWithoutHeaders = Arrays.copyOfRange(receivedData, i, receivedData.length);
            ByteArrayOutputStream outputStreamWithoutHeaders = new ByteArrayOutputStream();
            outputStreamWithoutHeaders.write(dataWithoutHeaders);
            fileManager.writePart(fileHash.toString(), partFileName.toString(), outputStreamWithoutHeaders);
            message = "From: "+ datagramPacket.getAddress() + ":" + datagramPacket.getPort() + " FileHash: " + fileHash + " : PartHash: " + partFileName + " Content:" + outputStreamWithoutHeaders.size();
            return message;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(PapayaFile papayaFile) {
        papayaFile.getPartFiles().forEach(partFile -> {
            Path partFilePath = storePath.resolve(papayaFile.getFileId())
                    .resolve(partFile.getFileName());
            if (partFilePath.toFile().exists()) {
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    outputStream.write(PeerMessageType.PART_FILE.getValue());
                    outputStream.write(papayaFile.getFileId().getBytes());
                    outputStream.write(partFile.getFileName().getBytes());
                    outputStream.write("#".getBytes());
                    outputStream.write(Files.readAllBytes(partFilePath));
                    byte[] partByte = outputStream.toByteArray();
                    socket.send(new DatagramPacket(partByte, partByte.length, InetAddress.getByName("localhost"), port));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("not found");
            }
        });

    }

    @Override
    public void stop() {
        socket.close();
        try {
            gatewayDevice.deletePortMapping(port, "UDP");
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

}
