package dev.tronxi.papayaclient.peer;

import dev.tronxi.papayaclient.persistence.papayastatusfile.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.logging.Logger;

@Service
public class AskForPartFileSender {

    Logger logger = Logger.getLogger(AskForPartFileSender.class.getName());

    @Value("${papaya.port}")
    protected int port;

    private static final Map<Peer, Long> peerAskedFiles = new HashMap<>();
    private static final Map<Long, PapayaStatus> partStatus = new HashMap<>();

    public void send(PapayaStatusFile papayaStatusFile) {
        logger.info("Ask for part file started: " + papayaStatusFile.getFileId());

        papayaStatusFile.getPartStatusFiles().stream()
                .filter(partStatusFile -> partStatusFile.getStatus().equals(PapayaStatus.INCOMPLETE))
                .filter(status -> partStatus.getOrDefault(status.getId(), PapayaStatus.INCOMPLETE) == PapayaStatus.INCOMPLETE)
                .findFirst()
                .ifPresentOrElse(status -> {
                    logger.info("Asking process for: " + status.getFileName() + " with status: " + status.getStatus() + " id: " + status.getId());

                    Set<PartPeerStatusFile> partPeerStatusFiles = status.getPartPeerStatusFiles();
                    partPeerStatusFiles.forEach(partPeerStatusFile -> {
                        peerAskedFiles.putIfAbsent(partPeerStatusFile.getPeer(), 0L);
                    });

                    logger.info("PeerAskedFiles before: " + peerAskedFiles);

                    partPeerStatusFiles.stream()
                            .min(Comparator.comparingLong(partPeerStatusFile -> peerAskedFiles.getOrDefault(partPeerStatusFile.getPeer(), 0L)))
                            .ifPresentOrElse(partPeerStatusFile -> {
                                partStatus.put(status.getId(), PapayaStatus.ASKED);
                                peerAskedFiles.put(partPeerStatusFile.getPeer(), peerAskedFiles.get(partPeerStatusFile.getPeer()) + 1);
                                sendMessage(papayaStatusFile, status, partPeerStatusFiles, partPeerStatusFile);
                                logger.info("PeerAskedFiles after: " + peerAskedFiles);
                            }, () -> {
                                partStatus.put(status.getId(), PapayaStatus.ASKED);
                                send(papayaStatusFile);
                            });
                }, () -> {
                    logger.info("Removing part status");
                    papayaStatusFile.getPartStatusFiles().forEach(status -> {
                        partStatus.remove(status.getId());
                    });
                    if (papayaStatusFile.getStatus() != PapayaStatus.COMPLETE) {
                        logger.info("Retry send");
                        send(papayaStatusFile);
                    }
                });
    }


    private void sendMessage(PapayaStatusFile papayaStatusFile, PartStatusFile partStatusFile, Set<PartPeerStatusFile> partPeerStatusFileList, PartPeerStatusFile partPeerStatusFile) {
        logger.info("Sending message: ask for part file: " + papayaStatusFile.getFileId() + " partFileName: " + partStatusFile.getFileName() + " Peer: " + partPeerStatusFile);
        if (partPeerStatusFileList.isEmpty()) {
            partStatus.put(partStatusFile.getId(), PapayaStatus.ASKED);
            send(papayaStatusFile);
        }
        if (!partPeerStatusFileList.isEmpty()) {
            partPeerStatusFileList.remove(partPeerStatusFile);
            try (Socket socket = new Socket(partPeerStatusFile.getPeer().address(), partPeerStatusFile.getPeer().port());
                 OutputStream outputStream = socket.getOutputStream()) {
                ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                dataStream.write(PeerMessageType.ASK_FOR_PART_FILE.getValue());
                dataStream.write(papayaStatusFile.getFileId().getBytes());
                dataStream.write(partStatusFile.getFileName().getBytes());
                dataStream.write("#".getBytes());
                dataStream.write(String.valueOf(port).getBytes());
                dataStream.write("#".getBytes());
                outputStream.write(dataStream.toByteArray());
                partPeerStatusFile.setPartPeerStatus(PartPeerStatus.ASKED);
                partPeerStatusFile.setLatestUpdateTime(System.currentTimeMillis());

            } catch (IOException e) {
                logger.severe(e.getMessage());
                partPeerStatusFileList.stream()
                        .filter(pf -> !pf.equals(partPeerStatusFile))
                        .min(Comparator.comparingLong(pf -> peerAskedFiles.getOrDefault(pf.getPeer(), 0L)))
                        .ifPresent(pf -> sendMessage(papayaStatusFile, partStatusFile, partPeerStatusFileList, pf));
            }
        }

    }
}
