package dev.tronxi.papayaclient.peer.services;

import dev.tronxi.papayaclient.peer.PeerMessageType;
import dev.tronxi.papayaclient.peer.handlers.AskForPartFileHandler;
import dev.tronxi.papayaclient.peer.handlers.AskForResourcesHandler;
import dev.tronxi.papayaclient.peer.handlers.PartFileHandler;
import dev.tronxi.papayaclient.peer.handlers.ResponseAskForResourcesHandler;
import org.springframework.stereotype.Service;

import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class HandlerService {
    Logger logger = Logger.getLogger(HandlerService.class.getName());

    private final AskForResourcesHandler askForResourcesHandler;
    private final ResponseAskForResourcesHandler responseAskForResourcesHandler;
    private final AskForPartFileHandler askForPartFileHandler;
    private final PartFileHandler partFileHandler;

    public HandlerService(AskForResourcesHandler askForResourcesHandler, ResponseAskForResourcesHandler responseAskForResourcesHandler, AskForPartFileHandler askForPartFileHandler, PartFileHandler partFileHandler) {
        this.askForResourcesHandler = askForResourcesHandler;
        this.responseAskForResourcesHandler = responseAskForResourcesHandler;
        this.askForPartFileHandler = askForPartFileHandler;
        this.partFileHandler = partFileHandler;
    }

    public CompletableFuture<String> handle(Socket clientSocket, byte[] receivedData) {
        CompletableFuture<String> message;
        PeerMessageType peerMessageType = PeerMessageType.fromValue(receivedData[0]);
        logger.info("Receiving: " + peerMessageType);
        switch (peerMessageType) {
            case PART_FILE -> message = partFileHandler.handleInNewThread(clientSocket, receivedData);
            case ASK_FOR_RESOURCES -> message = askForResourcesHandler.handleInNewThread(clientSocket, receivedData);
            case RESPONSE_ASK_FOR_RESOURCES ->
                    message = responseAskForResourcesHandler.handleInNewThread(clientSocket, receivedData);
            case ASK_FOR_PART_FILE -> message = askForPartFileHandler.handleInNewThread(clientSocket, receivedData);
            default -> message = CompletableFuture.completedFuture("Invalid");
        }
        return message;
    }
}
