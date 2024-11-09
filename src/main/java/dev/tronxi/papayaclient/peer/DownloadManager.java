package dev.tronxi.papayaclient.peer;

import dev.tronxi.papayaclient.persistence.FileManager;
import dev.tronxi.papayaclient.persistence.papayafile.PapayaFile;
import dev.tronxi.papayaclient.persistence.services.PapayaStatusFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

@Service
public class DownloadManager {

    @Value("${papaya.port}")
    private int port;

    private static final Logger logger = Logger.getLogger(DownloadManager.class.getName());

    private final FileManager fileManager;
    private final PeerSignalingService peerSignalingService;
    private final PapayaStatusFileService papayaStatusFileService;

    public DownloadManager(FileManager fileManager, PeerSignalingService peerSignalingService, PapayaStatusFileService papayaStatusFileService) {
        this.fileManager = fileManager;
        this.peerSignalingService = peerSignalingService;
        this.papayaStatusFileService = papayaStatusFileService;
    }

    public void startAllIncompleteDownloads() {
        papayaStatusFileService.findAllIncomplete().forEach(status -> {
            fileManager.retrievePapayaFileFromFileId(status.getFileId()).ifPresent(this::download);
        });
    }


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
