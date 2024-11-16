package dev.tronxi.papayaclient.peer.handlers;

import dev.tronxi.papayaclient.persistence.FileManager;
import dev.tronxi.papayaclient.peer.Peer;
import dev.tronxi.papayaclient.peer.PeerMessageType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Service
public class AskForResourcesHandler extends Handler {

    private static final Logger logger = Logger.getLogger(AskForResourcesHandler.class.getName());


    protected AskForResourcesHandler(FileManager fileManager) {
        super(fileManager);
    }

    @Override
    public String handle(Socket clientSocket, byte[] receivedData) {
        String message = "From: " + clientSocket.getInetAddress();
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
            Peer peer = new Peer(clientSocket.getInetAddress().getHostAddress(), Integer.parseInt(port.toString()));
            message += ":" + port + " AskForResources with fileId: " + fileId;
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
                dataStream.write(String.valueOf(port).getBytes());
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
}
