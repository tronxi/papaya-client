package dev.tronxi.papayaclient.peer;

import dev.tronxi.papayaclient.persistence.papayafile.PapayaFile;
import dev.tronxi.papayaclient.peer.handlers.AskForPartFileHandler;
import dev.tronxi.papayaclient.peer.handlers.AskForResourcesHandler;
import dev.tronxi.papayaclient.peer.handlers.PartFileHandler;
import dev.tronxi.papayaclient.peer.handlers.ResponseAskForResourcesHandler;
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
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PeerConnectionManagerTCP implements PeerConnectionManager {

    private static final Logger logger = Logger.getLogger(PeerConnectionManagerTCP.class.getName());

    @Value("${papaya.port}")
    private int port;

    private ServerSocket serverSocket;

    private final DownloadManager downloadManager;
    private final GatewayDevice gatewayDevice;
    private final AskForResourcesHandler askForResourcesHandler;
    private final ResponseAskForResourcesHandler responseAskForResourcesHandler;
    private final AskForPartFileHandler askForPartFileHandler;
    private final PartFileHandler partFileHandler;

    public PeerConnectionManagerTCP(GatewayDevice gatewayDevice, DownloadManager downloadManager, AskForResourcesHandler askForResourcesHandler, ResponseAskForResourcesHandler responseAskForResourcesHandler, AskForPartFileHandler askForPartFileHandler, PartFileHandler partFileHandler) {
        this.downloadManager = downloadManager;
        this.askForResourcesHandler = askForResourcesHandler;
        this.responseAskForResourcesHandler = responseAskForResourcesHandler;
        this.askForPartFileHandler = askForPartFileHandler;
        this.partFileHandler = partFileHandler;
        this.gatewayDevice = gatewayDevice;
        logger.setLevel(Level.INFO);
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
                                CompletableFuture<String> message = null;
                                PeerMessageType peerMessageType = PeerMessageType.fromValue(typeByte);
                                logger.info("Receiving: " + peerMessageType);
                                switch (peerMessageType) {
                                    case PART_FILE ->
                                            message = partFileHandler.handleInNewThread(clientSocket, receivedData);
                                    case ASK_FOR_RESOURCES ->
                                            message = askForResourcesHandler.handleInNewThread(clientSocket, receivedData);
                                    case RESPONSE_ASK_FOR_RESOURCES ->
                                            message = responseAskForResourcesHandler.handleInNewThread(clientSocket, receivedData);
                                    case ASK_FOR_PART_FILE ->
                                            message = askForPartFileHandler.handleInNewThread(clientSocket, receivedData);
                                    default -> message = CompletableFuture.completedFuture("Invalid");
                                }
                                CompletableFuture<String> finalMessage = message;
                                finalMessage.thenAcceptAsync(string -> {
                                    Platform.runLater(() -> {
                                        textArea.appendText("\n" + string);
                                        String[] lines = textArea.getText().split("\n");
                                        if (lines.length > 100) {
                                            String newText = String.join("\n", Arrays.copyOfRange(lines, lines.length - 100, lines.length));
                                            textArea.setText(newText);
                                            textArea.appendText("");
                                        }
                                    });
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
        downloadManager.download(papayaFile);
    }

    @Override
    public void startAllIncompleteDownloads() {
        downloadManager.startAllIncompleteDownloads();
    }
}
