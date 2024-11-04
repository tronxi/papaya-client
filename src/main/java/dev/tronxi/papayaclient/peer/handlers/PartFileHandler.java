package dev.tronxi.papayaclient.peer.handlers;

import dev.tronxi.papayaclient.files.FileManager;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

@Service
public class PartFileHandler extends Handler {
    private static final Logger logger = Logger.getLogger(PartFileHandler.class.getName());

    protected PartFileHandler(FileManager fileManager) {
        super(fileManager);
    }

    @Override
    public String handle(Socket clientSocket, byte[] receivedData) {
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
}
