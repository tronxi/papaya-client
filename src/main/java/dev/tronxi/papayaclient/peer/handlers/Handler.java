package dev.tronxi.papayaclient.peer.handlers;

import dev.tronxi.papayaclient.files.FileManager;
import org.springframework.beans.factory.annotation.Value;

import java.net.Socket;

public abstract class Handler {
    @Value("${papaya.port}")
    protected int port;

    protected final FileManager fileManager;

    protected Handler(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public abstract String handle(Socket clientSocket, byte[] receivedData);
}
