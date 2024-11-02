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

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class PeerConnectionManagerTCP implements PeerConnectionManager {

    private final FileManager fileManager;
    @Value("${papaya.port}")
    private int port;

    @Value("${papaya.workspace}")
    private String workspace;
    private Path storePath;

    private ServerSocket serverSocket;
    private final GatewayDevice gatewayDevice;

    public PeerConnectionManagerTCP(GatewayDevice gatewayDevice, FileManager fileManager) {
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
            serverSocket = new ServerSocket(port);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    while (true) {
                        try (Socket clientSocket = serverSocket.accept();
                             InputStream inputStream = clientSocket.getInputStream();
                             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                            byte[] buffer = new byte[80000];
                            int length;
                            while ((length = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, length);
                            }

                            if (outputStream.size() > 0) {
                                byte[] receivedData = outputStream.toByteArray();
                                int typeByte = receivedData[0];
                                String message = "";
                                PeerMessageType peerMessageType = PeerMessageType.fromValue(typeByte);
                                switch (peerMessageType) {
                                    case PART_FILE -> {
                                        message = receivePartFile(clientSocket, receivedData);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String receivePartFile(Socket clientSocket, byte[] receivedData) {
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
            message = "From: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() +
                    " FileHash: " + fileHash + " : PartHash: " + partFileName + " Content: " + outputStreamWithoutHeaders.size();
            return message;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(PapayaFile papayaFile) {
        papayaFile.getPartFiles().forEach(partFile -> {
            Path partFilePath = storePath.resolve(papayaFile.getFileHash())
                    .resolve(partFile.getFileName());
            if (partFilePath.toFile().exists()) {
                try (Socket socket = new Socket("localhost", port);
                     OutputStream outputStream = socket.getOutputStream()) {

                    ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                    dataStream.write(PeerMessageType.PART_FILE.getValue());
                    dataStream.write(papayaFile.getFileHash().getBytes());
                    dataStream.write(partFile.getFileName().getBytes());
                    dataStream.write("#".getBytes());
                    dataStream.write(Files.readAllBytes(partFilePath));
                    byte[] partByte = dataStream.toByteArray();
                    outputStream.write(partByte);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("File not found: " + partFilePath);
            }
        });
    }

    @Override
    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            gatewayDevice.deletePortMapping(port, "TCP");
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
