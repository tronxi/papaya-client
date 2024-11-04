package dev.tronxi.papayaclient.peer.handlers;

import dev.tronxi.papayaclient.files.FileManager;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ResponseAskForResourcesHandler extends Handler {

    private static final Logger logger = Logger.getLogger(ResponseAskForResourcesHandler.class.getName());

    protected ResponseAskForResourcesHandler(FileManager fileManager) {
        super(fileManager);
    }

    @Override
    public String handle(Socket clientSocket, byte[] receivedData) {
        String message = "From: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort();
        ByteArrayOutputStream fileId = new ByteArrayOutputStream();
        try {
            fileId.write(Arrays.copyOfRange(receivedData, 1, 33));
            int i = 34;
            List<String> completedParts = new ArrayList<>();
            ByteArrayOutputStream port = new ByteArrayOutputStream();
            int charAtIndex;
            do {
                charAtIndex = (char) receivedData[i];
                if (charAtIndex != '#') {
                    port.write(receivedData[i]);
                }
                i++;
            } while (charAtIndex != '#');
            do {
                ByteArrayOutputStream part = new ByteArrayOutputStream();
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
            message += " ResponseAskForResources with fileId: " + fileId + " Port: " + port + " parts: " + completedParts.size();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return message;
    }
}
