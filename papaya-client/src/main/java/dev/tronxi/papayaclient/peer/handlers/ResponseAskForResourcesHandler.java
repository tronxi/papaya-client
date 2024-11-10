package dev.tronxi.papayaclient.peer.handlers;

import dev.tronxi.papayaclient.persistence.FileManager;
import dev.tronxi.papayaclient.persistence.papayastatusfile.*;
import dev.tronxi.papayaclient.peer.AskForPartFileSender;
import dev.tronxi.papayaclient.peer.Peer;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class ResponseAskForResourcesHandler extends Handler {

    private static final Logger logger = Logger.getLogger(ResponseAskForResourcesHandler.class.getName());

    private final AskForPartFileSender askForPartFileSender;


    protected ResponseAskForResourcesHandler(FileManager fileManager, AskForPartFileSender askForPartFileSender) {
        super(fileManager);
        this.askForPartFileSender = askForPartFileSender;
    }

    @Override
    public String handle(Socket clientSocket, byte[] receivedData) {
        String message = "From: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort();
        ByteArrayOutputStream fileId = new ByteArrayOutputStream();
        try {
            fileId.write(Arrays.copyOfRange(receivedData, 1, 33));
            int i = 33;
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
            Peer peer = new Peer(clientSocket.getInetAddress().getHostAddress(), Integer.parseInt(port.toString()));
            Optional<PapayaStatusFile> maybePapayaStatusFileUpdated = updateStatus(fileId.toString(), peer, completedParts);
            maybePapayaStatusFileUpdated.ifPresent(askForPartFileSender::send);
            message += " ResponseAskForResources with fileId: " + fileId + " Port: " + port + " parts: " + completedParts.size();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return message;
    }

    private Optional<PapayaStatusFile> updateStatus(String fileId, Peer peer, List<String> completedParts) {
        logger.info("Updating status for: " + fileId + " Peer: " + peer);
        Optional<PapayaStatusFile> maybePapayaStatusFile = fileManager.retrievePapayaStatusFileFromFile(fileId);
        if (maybePapayaStatusFile.isPresent()) {
            PapayaStatusFile papayaStatusFile = maybePapayaStatusFile.get();
            boolean statusChanged = false;
            for (PartStatusFile partStatusFile : papayaStatusFile.getPartStatusFiles()) {
                if (completedParts.contains(partStatusFile.getFileName())) {
                    if (partStatusFile.getStatus().equals(PapayaStatus.INCOMPLETE)) {
                        PartPeerStatusFile partPeerStatusFile = new PartPeerStatusFile(peer, PartPeerStatus.NO_ASKED, System.currentTimeMillis());
                        partStatusFile.addPeer(partPeerStatusFile);
                    }
                    statusChanged = true;
                }
            }
            if (statusChanged) {
                fileManager.savePapayaStatusFile(papayaStatusFile);
            }
            return Optional.of(papayaStatusFile);
        }
        return Optional.empty();
    }
}
