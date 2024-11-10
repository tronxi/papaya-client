package dev.tronxi.papayaclient.peer.services;

import dev.tronxi.papayaclient.peer.Peer;
import dev.tronxi.papayaclient.peer.PeerMessageType;
import dev.tronxi.papayaclient.persistence.FileManager;
import dev.tronxi.papayaclient.persistence.papayafile.PapayaFile;
import dev.tronxi.papayaclient.persistence.services.PapayaStatusFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Service
public class DownloadService {

    @Value("${papaya.port}")
    private int port;

    private static final Logger logger = Logger.getLogger(DownloadService.class.getName());

    private final FileManager fileManager;
    private final PeerTrackerService peerTrackerService;
    private final PapayaStatusFileService papayaStatusFileService;

    public DownloadService(FileManager fileManager, PeerTrackerService peerTrackerService, PapayaStatusFileService papayaStatusFileService) {
        this.fileManager = fileManager;
        this.peerTrackerService = peerTrackerService;
        this.papayaStatusFileService = papayaStatusFileService;
    }

    public void startAllIncompleteDownloads() {
        papayaStatusFileService.findAllIncomplete().forEach(status -> {
            status.getPartStatusFiles().forEach(partStatusFile -> {
                partStatusFile.setPartPeerStatusFiles(Collections.emptySet());
            });
            fileManager.savePapayaStatusFile(status);
            fileManager.retrievePapayaFileFromFileId(status.getFileId()).ifPresent(this::download);
        });
    }


    public void download(PapayaFile papayaFile) {
        logger.info("Downloading file... " + papayaFile.getFileName());
        fileManager.createStoreFromPapayaFile(papayaFile);
        List<Peer> peers = peerTrackerService.retrievePeers();
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
