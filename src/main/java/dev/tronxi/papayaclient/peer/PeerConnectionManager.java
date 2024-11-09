package dev.tronxi.papayaclient.peer;

import dev.tronxi.papayaclient.persistence.papayafile.PapayaFile;
import javafx.scene.control.TextArea;


public interface PeerConnectionManager {
    void start(TextArea textArea);
    void stop();
    void download(PapayaFile papayaFile);
    void startAllIncompleteDownloads();
}
