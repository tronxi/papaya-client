package dev.tronxi.papayaclient.peer;

import dev.tronxi.papayaclient.files.FileManager;
import dev.tronxi.papayaclient.files.papayafile.PapayaFile;
import dev.tronxi.papayaclient.peer.handlers.AskForPartFileHandler;
import dev.tronxi.papayaclient.peer.handlers.AskForResourcesHandler;
import dev.tronxi.papayaclient.peer.handlers.PartFileHandler;
import dev.tronxi.papayaclient.peer.handlers.ResponseAskForResourcesHandler;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PeerConnectionManagerTCP implements PeerConnectionManager {

    private static final Logger logger = Logger.getLogger(PeerConnectionManagerTCP.class.getName());

    @Value("${papaya.port}")
    private int port;

    @Value("${papaya.workspace}")
    private String workspace;

    private Path storePath;

    private ServerSocket serverSocket;

    private final FileManager fileManager;
    private final GatewayDevice gatewayDevice;
    private final PeerSignalingService peerSignalingService;
    private final AskForResourcesHandler askForResourcesHandler;
    private final ResponseAskForResourcesHandler responseAskForResourcesHandler;
    private final AskForPartFileHandler askForPartFileHandler;
    private final PartFileHandler partFileHandler;

    public PeerConnectionManagerTCP(GatewayDevice gatewayDevice, FileManager fileManager, PeerSignalingService peerSignalingService, AskForResourcesHandler askForResourcesHandler, ResponseAskForResourcesHandler responseAskForResourcesHandler, AskForPartFileHandler askForPartFileHandler, PartFileHandler partFileHandler) {
        this.askForResourcesHandler = askForResourcesHandler;
        this.responseAskForResourcesHandler = responseAskForResourcesHandler;
        this.askForPartFileHandler = askForPartFileHandler;
        this.partFileHandler = partFileHandler;
        this.gatewayDevice = gatewayDevice;
        this.fileManager = fileManager;
        this.peerSignalingService = peerSignalingService;
        logger.setLevel(Level.INFO);
    }

    @PostConstruct
    public void init() {
        storePath = Path.of(workspace + "/store/");
    }

    @Override
    public void start(TextArea textArea) {
        logger.info("Start peer connection manager tcp");
        try {
            serverSocket = new ServerSocket(port);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    while (true) {
                        try (Socket clientSocket = serverSocket.accept();
                             InputStream inputStream = clientSocket.getInputStream();
                             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                            logger.info("Receiving...");

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
                                logger.info("Receiving: " + peerMessageType);
                                switch (peerMessageType) {
                                    case PART_FILE -> message = partFileHandler.handle(clientSocket, receivedData);
                                    case ASK_FOR_RESOURCES ->
                                            message = askForResourcesHandler.handle(clientSocket, receivedData);
                                    case RESPONSE_ASK_FOR_RESOURCES ->
                                            message = responseAskForResourcesHandler.handle(clientSocket, receivedData);
                                    case ASK_FOR_PART_FILE -> askForPartFileHandler.handle(clientSocket, receivedData);
                                    case INVALID -> message = "Invalid";
                                }
                                String finalMessage = message;
                                Platform.runLater(() -> {
                                    textArea.appendText("\n" + finalMessage);
                                    String[] lines = textArea.getText().split("\n");
                                    if (lines.length > 100) {
                                        String newText = String.join("\n", Arrays.copyOfRange(lines, lines.length - 100, lines.length));
                                        textArea.setText(newText);
                                        textArea.appendText("");
                                    }
                                });
                            } else {
                                logger.info("Received empty message");
                            }
                        } catch (IOException e) {
                            logger.severe(e.getMessage());
                            return null;
                        }
                    }
                }
            };
            new Thread(task).start();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }

    @Override
    public void send(PapayaFile papayaFile) {
        logger.info("Sending file...");
        papayaFile.getPartFiles().forEach(partFile -> {
            Path partFilePath = storePath.resolve(papayaFile.getFileId())
                    .resolve(partFile.getFileName());
            if (partFilePath.toFile().exists()) {
                try (Socket socket = new Socket("localhost", port);
                     OutputStream outputStream = socket.getOutputStream()) {

                    ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                    dataStream.write(PeerMessageType.PART_FILE.getValue());
                    dataStream.write(papayaFile.getFileId().getBytes());
                    dataStream.write(partFile.getFileName().getBytes());
                    dataStream.write("#".getBytes());
                    dataStream.write(Files.readAllBytes(partFilePath));
                    outputStream.write(dataStream.toByteArray());
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }
            } else {
                logger.severe("File not found: " + partFilePath);
            }
        });
    }

    @Override
    public void stop() {
        logger.info("Stop...");
        try {
            logger.info("Delete port mapping");
            gatewayDevice.deletePortMapping(port, "TCP");
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

    @Override
    public void download(PapayaFile papayaFile) {
        logger.info("Downloading file... " + papayaFile.getFileName());
        fileManager.createStoreFromPapayaFile(papayaFile);
        List<Peer> peers = peerSignalingService.retrievePeers();
        logger.info("Peers retrieved: " + peers.size());
        peers.forEach(peer -> {
            askForResources(papayaFile, peer);
        });
    }

    private void askForResources(PapayaFile papayaFile, Peer peer) {
        logger.info("Asking for resources: " + papayaFile.getFileName() + " for " + peer);
        try (Socket socket = new Socket(peer.address(), peer.port());
             OutputStream outputStream = socket.getOutputStream()) {
            ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            dataStream.write(PeerMessageType.ASK_FOR_RESOURCES.getValue());
            dataStream.write(papayaFile.getFileId().getBytes());
            dataStream.write(String.valueOf(port).getBytes());
            dataStream.write("#".getBytes());
            outputStream.write(dataStream.toByteArray());
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }
}
