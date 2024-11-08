package dev.tronxi.papayaclient.peer.handlers;

import dev.tronxi.papayaclient.files.FileManager;
import dev.tronxi.papayaclient.peer.Peer;
import dev.tronxi.papayaclient.peer.PeerMessageType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

@Service
public class AskForPartFileHandler extends Handler {

    private static final Logger logger = Logger.getLogger(AskForPartFileHandler.class.getName());

    protected AskForPartFileHandler(FileManager fileManager) {
        super(fileManager);
    }

    @Override
    public String handle(Socket clientSocket, byte[] receivedData) {
        String message = "From: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort();
        try {
            ByteArrayOutputStream fileId = new ByteArrayOutputStream();
            fileId.write(Arrays.copyOfRange(receivedData, 1, 33));
            ByteArrayOutputStream part = new ByteArrayOutputStream();
            int i = 33;
            int charAtIndex;
            do {
                charAtIndex = (char) receivedData[i];
                if (charAtIndex != '#') {
                    part.write(receivedData[i]);
                }
                i++;
            } while (charAtIndex != '#');
            ByteArrayOutputStream port = new ByteArrayOutputStream();
            do {
                charAtIndex = (char) receivedData[i];
                if (charAtIndex != '#') {
                    port.write(receivedData[i]);
                }
                i++;
            } while (charAtIndex != '#');
            message += " AskForPartFile with fileId: " + fileId + " Part: " + part + " Port: " + port;
            sendPartFile(clientSocket, fileId, part, port);
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return message;
    }

    private void sendPartFile(Socket clientSocket, ByteArrayOutputStream fileId, ByteArrayOutputStream part, ByteArrayOutputStream port) {
        logger.info("Sending part: " + part.toString() + " fileId: " + fileId.toString());
        Path partFilePath = storePath.resolve(fileId.toString()).resolve(part.toString());
        if (partFilePath.toFile().exists()) {
            Peer peer = new Peer(clientSocket.getInetAddress().getHostAddress(), Integer.parseInt(port.toString()));
            try (Socket socket = new Socket(peer.address(), peer.port());
                 OutputStream outputStream = socket.getOutputStream()) {

                ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                dataStream.write(PeerMessageType.PART_FILE.getValue());
                dataStream.write(fileId.toString().getBytes());
                dataStream.write(part.toString().getBytes());
                dataStream.write("#".getBytes());
                dataStream.write(Files.readAllBytes(partFilePath));
                outputStream.write(dataStream.toByteArray());
            } catch (IOException e) {
                logger.severe(e.getMessage());
            }
        } else {
            logger.severe("File not found: " + partFilePath);
        }
    }
}
