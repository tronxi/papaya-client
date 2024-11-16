package dev.tronxi.papayaclient.peer.handlers;

import dev.tronxi.papayaclient.persistence.FileManager;
import dev.tronxi.papayaclient.persistence.HashGenerator;
import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatus;
import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatusFile;
import dev.tronxi.papayaclient.peer.AskForPartFileSender;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class PartFileHandler extends Handler {
    private static final Logger logger = Logger.getLogger(PartFileHandler.class.getName());
    private final HashGenerator hashGenerator;
    private final AskForPartFileSender askForPartFileSender;
    private static final Map<String, PapayaStatus> filesJoined = new HashMap<>();

    protected PartFileHandler(FileManager fileManager, HashGenerator hashGenerator, AskForPartFileSender askForPartFileSender) {
        super(fileManager);
        this.hashGenerator = hashGenerator;
        this.askForPartFileSender = askForPartFileSender;
    }

    @Override
    @Transactional
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
            Optional<PapayaStatusFile> maybePapayaStatusFile = fileManager.retrievePapayaStatusFileFromFile(fileId.toString());
            if (maybePapayaStatusFile.isPresent()) {
                PapayaStatusFile statusFile = maybePapayaStatusFile.get();
                statusFile.getPartStatusFiles().stream()
                        .filter(partStatusFile -> partStatusFile.getFileName().equals(partFileName.toString()))
                        .findFirst()
                        .ifPresent(partStatusFile -> {
                            String hash = hashGenerator.generateHash(outputStreamWithoutHeaders.toByteArray());
                            if (hash.equals(partStatusFile.getFileHash())) {
                                fileManager.writePart(fileId.toString(), partFileName.toString(), outputStreamWithoutHeaders);
                                partStatusFile.setStatus(PapayaStatus.COMPLETE);
                                fileManager.savePartStatusFile(statusFile, partStatusFile);
                                if (statusFile.getStatus() == PapayaStatus.COMPLETE) {
                                    if (!filesJoined.containsKey(statusFile.getFileId())) {
                                        filesJoined.put(statusFile.getFileId(), PapayaStatus.JOINED);
                                        Optional<Path> maybePath = fileManager.joinStore(storePath.resolve(fileId.toString()).toFile());
                                        maybePath.ifPresentOrElse((path -> logger.info("File downloaded: " + path)),
                                                () -> {
                                                    logger.severe("Error ");
                                                });
                                    }
                                }
                            } else {
                                logger.severe("Invalid hash");
                            }
                        });
                if (statusFile.getStatus() != PapayaStatus.COMPLETE && !filesJoined.containsKey(statusFile.getFileId())) {
                    askForPartFileSender.send(statusFile);
                }
            } else {
                logger.severe("Could not find PapayaStatusFile for " + fileId);
            }
            message = "From: " + clientSocket.getInetAddress() +
                    " FileId: " + fileId + " : Part: " + partFileName + " Content: " + outputStreamWithoutHeaders.size();
            return message;
        } catch (IOException e) {
            logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
