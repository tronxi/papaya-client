package dev.tronxi.papayaclient.peer.handlers;

import dev.tronxi.papayaclient.files.FileManager;
import org.springframework.stereotype.Service;

import java.net.Socket;

@Service
public class AskForPartFileHandler extends Handler {

    protected AskForPartFileHandler(FileManager fileManager) {
        super(fileManager);
    }

    @Override
    public String handle(Socket clientSocket, byte[] receivedData) {
        return "";
    }
}
