package dev.tronxi.papayaclient.peer;

import dev.tronxi.papayaclient.peer.services.DownloadService;
import dev.tronxi.papayaclient.peer.services.HandlerService;
import dev.tronxi.papayaclient.peer.services.StopService;
import dev.tronxi.papayaclient.persistence.papayafile.PapayaFile;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    private final DownloadService downloadService;
    private final StopService stopService;
    private final HandlerService handlerService;

    public PeerConnectionManagerTCP(DownloadService downloadService, StopService stopService, HandlerService handlerService) {
        this.downloadService = downloadService;
        this.stopService = stopService;
        this.handlerService = handlerService;
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
                                CompletableFuture<String> message = handlerService.handle(clientSocket, receivedData);
                                message.thenAcceptAsync(string -> {
                                    Platform.runLater(() -> {
                                        textArea.appendText("\n" + string);
                                        String[] lines = textArea.getText().split("\n");
                                        if (lines.length > 100) {
                                            String newText = String.join("\n", Arrays.copyOfRange(lines, lines.length - 100, lines.length));
                                            textArea.appendText(newText);
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
        this.stopService.stop(serverSocket);
    }

    @Override
    public void download(PapayaFile papayaFile) {
        downloadService.download(papayaFile);
    }

    @Override
    public void startAllIncompleteDownloads() {
        downloadService.startAllIncompleteDownloads();
    }
}
