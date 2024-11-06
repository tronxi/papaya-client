package dev.tronxi.papayaclient.peer.handlers;

import dev.tronxi.papayaclient.files.FileManager;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import java.net.Socket;
import java.nio.file.Path;

public abstract class Handler {
    @Value("${papaya.port}")
    protected int port;

    @Value("${papaya.workspace}")
    private String workspace;

    protected Path storePath;

    protected final FileManager fileManager;

    protected Handler(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @PostConstruct
    public void init() {
        storePath = Path.of(workspace + "/store/");
    }

    public abstract String handle(Socket clientSocket, byte[] receivedData);
}
