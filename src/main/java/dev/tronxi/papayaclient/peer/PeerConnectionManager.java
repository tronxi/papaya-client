package dev.tronxi.papayaclient.peer;

import dev.tronxi.papayaclient.files.papayafile.PapayaFile;
import javafx.scene.control.TextArea;


public interface PeerConnectionManager {
    void start(TextArea textArea);
    void send(PapayaFile papayaFile);
    void stop();
}
