package dev.tronxi.papayaclient.peer;

import dev.tronxi.papayaclient.files.FileManager;
import dev.tronxi.papayaclient.files.papayastatusfile.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class AskForPartFileSender {

    Logger logger = Logger.getLogger(AskForPartFileSender.class.getName());

    @Value("${papaya.port}")
    protected int port;

    private final FileManager fileManager;

    public AskForPartFileSender(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void send(PapayaStatusFile papayaStatusFile) {
        logger.info("Ask for part file started: " + papayaStatusFile.getFileId());
        papayaStatusFile.getPartStatusFiles().stream()
                .filter(partStatusFile -> partStatusFile.getStatus().equals(PapayaStatus.INCOMPLETE))
                .forEach(status -> {
                    Set<PartPeerStatusFile> partPeerStatusFiles = status.getPartPeerStatusFiles();
                    boolean partDownloaded = checkIfPartisDownloaded(partPeerStatusFiles);
                    if (partDownloaded) {
                        status.setStatus(PapayaStatus.COMPLETE);
                    } else {
                        updateAskedToTimeout(partPeerStatusFiles);
                        boolean partAsked = checkIfPartIdAsked(partPeerStatusFiles);
                        if (!partAsked) {
                            askToRandomNoAsked(papayaStatusFile.getFileId(),status.getFileName(), partPeerStatusFiles);
                        }
                    }
                });

        fileManager.savePapayaStatusFile(papayaStatusFile.getFileId(), papayaStatusFile);
    }

    private boolean checkIfPartisDownloaded(Set<PartPeerStatusFile> partPeerStatusFiles) {
        return partPeerStatusFiles.stream()
                .anyMatch(partPeerStatusFile -> partPeerStatusFile.getPartPeerStatus().equals(PartPeerStatus.DOWNLOADED));
    }

    private void updateAskedToTimeout(Set<PartPeerStatusFile> partPeerStatusFiles) {
        partPeerStatusFiles.stream()
                .filter(partPeerStatusFile -> partPeerStatusFile.getPartPeerStatus().equals(PartPeerStatus.ASKED))
                .forEach(partPeerStatusFile -> {
                    long differenceInMinutes = calculateDifferenceInMinutes(partPeerStatusFile.getLatestUpdateTime(), System.currentTimeMillis());
                    if (differenceInMinutes > 10) {
                        partPeerStatusFile.setPartPeerStatus(PartPeerStatus.TIMEOUT);
                        partPeerStatusFile.setLatestUpdateTime(System.currentTimeMillis());
                    }
                });
    }

    private boolean checkIfPartIdAsked(Set<PartPeerStatusFile> partPeerStatusFiles) {
        return partPeerStatusFiles.stream()
                .anyMatch(partPeerStatusFile -> partPeerStatusFile.getPartPeerStatus().equals(PartPeerStatus.ASKED));
    }

    private void askToRandomNoAsked(String fileId, String partFileName, Set<PartPeerStatusFile> partPeerStatusFiles) {
        partPeerStatusFiles.stream()
                .filter(partPeerStatusFile -> partPeerStatusFile.getPartPeerStatus().equals(PartPeerStatus.NO_ASKED))
                .findAny()
                .ifPresent(partPeerStatusFile -> {
                    sendMessage(fileId, partFileName, partPeerStatusFile);
                });
    }

    private void sendMessage(String fileId, String partFileName, PartPeerStatusFile partPeerStatusFile) {
        logger.info("Sending message: ask for part file: " + fileId + " partFileName: " + partFileName + " Peer: " + partPeerStatusFile);
        try (Socket socket = new Socket(partPeerStatusFile.getPeer().address(), partPeerStatusFile.getPeer().port());
             OutputStream outputStream = socket.getOutputStream()) {
            ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            dataStream.write(PeerMessageType.ASK_FOR_PART_FILE.getValue());
            dataStream.write(fileId.getBytes());
            dataStream.write(partFileName.getBytes());
            dataStream.write("#".getBytes());
            dataStream.write(String.valueOf(port).getBytes());
            outputStream.write(dataStream.toByteArray());
            partPeerStatusFile.setPartPeerStatus(PartPeerStatus.ASKED);
            partPeerStatusFile.setLatestUpdateTime(System.currentTimeMillis());

        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }

    private long calculateDifferenceInMinutes(long startTimeMillis, long endTimeMillis) {
        long differenceInMillis = endTimeMillis - startTimeMillis;
        return differenceInMillis / (1000 * 60);
    }
}
