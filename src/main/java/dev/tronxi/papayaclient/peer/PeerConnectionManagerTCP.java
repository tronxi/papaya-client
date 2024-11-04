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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PeerConnectionManagerTCP implements PeerConnectionManager {

    private static final Logger logger = Logger.getLogger(PeerConnectionManagerTCP.class.getName());


    private final FileManager fileManager;
    @Value("${papaya.port}")
    private int port;

    @Value("${papaya.workspace}")
    private String workspace;
    private Path storePath;

    private ServerSocket serverSocket;
    private final GatewayDevice gatewayDevice;
    private final PeerSignalingService peerSignalingService;

    public PeerConnectionManagerTCP(GatewayDevice gatewayDevice, FileManager fileManager, PeerSignalingService peerSignalingService) {
        logger.setLevel(Level.INFO);
        this.gatewayDevice = gatewayDevice;
        this.fileManager = fileManager;
        this.peerSignalingService = peerSignalingService;
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
                                    case PART_FILE -> {
                                        message = receivePartFile(clientSocket, receivedData);
                                    }
                                    case ASK_FOR_RESOURCES -> {
                                        message = receiveAskForResources(clientSocket, receivedData);
                                    }
                                    case RESPONSE_ASK_FOR_RESOURCES -> {
                                        message = receiveResponseAskForResources(clientSocket, receivedData);
                                    }
                                    case INVALID -> {
                                        message = "Invalid";
                                    }
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

    private String receiveAskForResources(Socket clientSocket, byte[] receivedData) {
        String message = "From: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort();
        try {
            ByteArrayOutputStream fileId = new ByteArrayOutputStream();
            fileId.write(Arrays.copyOfRange(receivedData, 1, 33));
            ByteArrayOutputStream port = new ByteArrayOutputStream();
            int i = 33;
            int charAtIndex;
            do {
                charAtIndex = (char) receivedData[i];
                if (charAtIndex != '#') {
                    port.write(receivedData[i]);
                }
                i++;
            } while (charAtIndex != '#');
            Peer peer = new Peer(clientSocket.getInetAddress().getHostAddress(), 3390);
            message += " AskForResources with fileId: " + fileId + " Port: " + port;
            responseAskForResources(peer, fileId.toString());
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return message;
    }

    private void responseAskForResources(Peer peer, String fileId) {
        logger.info("response ask for resources: " + fileId + " to " + peer);
        List<String> completedParts = fileManager.getCompletedParts(fileId);
        logger.info("found: " + completedParts.size() + " parts");
        if (!completedParts.isEmpty()) {
            try (Socket socket = new Socket(peer.address(), peer.port());
                 OutputStream outputStream = socket.getOutputStream()) {
                ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                dataStream.write(PeerMessageType.RESPONSE_ASK_FOR_RESOURCES.getValue());
                dataStream.write(fileId.getBytes());
                dataStream.write("#".getBytes());
                for (String completedPart : completedParts) {
                    dataStream.write(completedPart.getBytes());
                    dataStream.write("#".getBytes());
                }
                outputStream.write(dataStream.toByteArray());

            } catch (IOException e) {
                logger.severe(e.getMessage());
            }
        }
    }

    private String receiveResponseAskForResources(Socket clientSocket, byte[] receivedData) {
        String message = "From: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort();
        ByteArrayOutputStream fileId = new ByteArrayOutputStream();
        try {
            fileId.write(Arrays.copyOfRange(receivedData, 1, 33));
            int i = 34;
            List<String> completedParts = new ArrayList<>();
            do {
                ByteArrayOutputStream part = new ByteArrayOutputStream();
                int charAtIndex;
                do {
                    charAtIndex = (char) receivedData[i];
                    if (charAtIndex != '#') {
                        part.write(receivedData[i]);
                    }
                    i++;
                } while (charAtIndex != '#');
                completedParts.add(part.toString());
            } while (i < receivedData.length);
            logger.info("found: " + completedParts.size() + " parts");
            message += " ResponseAskForResources with fileId: " + fileId + " parts: " + completedParts.size();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return message;
    }

    private String receivePartFile(Socket clientSocket, byte[] receivedData) {
        String message;
        ByteArrayOutputStream fileId = new ByteArrayOutputStream();
        try {
            logger.info("Receiving part file...");
            fileId.write(Arrays.copyOfRange(receivedData, 1, 33));
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
            ByteArrayOutputStream outputStreamWithoutHeaders = new ByteArrayOutputStream();
            outputStreamWithoutHeaders.write(Arrays.copyOfRange(receivedData, i, receivedData.length));
            fileManager.writePart(fileId.toString(), partFileName.toString(), outputStreamWithoutHeaders);
            message = "From: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() +
                    " FileId: " + fileId + " : PartHash: " + partFileName + " Content: " + outputStreamWithoutHeaders.size();
            return message;
        } catch (IOException e) {
            logger.severe(e.getMessage());
            throw new RuntimeException(e);
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
