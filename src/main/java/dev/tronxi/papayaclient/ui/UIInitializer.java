package dev.tronxi.papayaclient.ui;

import dev.tronxi.papayaclient.PapayaClientApplication;
import dev.tronxi.papayaclient.files.FileManager;
import dev.tronxi.papayaclient.files.papayafile.PapayaFile;
import dev.tronxi.papayaclient.peer.PeerConnectionManager;
import dev.tronxi.papayaclient.ui.components.CreateDirectoryChooserButton;
import dev.tronxi.papayaclient.ui.components.CreateFileChooserButton;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class UIInitializer extends Application {

    private FileManager fileManager;
    private PeerConnectionManager peerConnectionManager;

    @Override
    public void init() {
        fileManager = PapayaClientApplication.getContext().getBean(FileManager.class);
        peerConnectionManager = PapayaClientApplication.getContext().getBean(PeerConnectionManager.class);
    }

    @Override
    public void start(Stage stage) {
        Button createPapayaFileButton = generateCreatePapayaFileButton(stage);
        Button joinButton = generateJoinButton(stage);
        Button statusButton = generateStatusButton(stage);
        Button sendButton = generateSendButton(stage);
        HBox hBox = new HBox(createPapayaFileButton, joinButton, statusButton, sendButton);

        TextArea logs = new TextArea();
        logs.setEditable(false);
        logs.setText("");
        logs.setMaxHeight(400);
        logs.setMinHeight(400);
        peerConnectionManager.start(logs);


        Scene scene = new Scene(new VBox(hBox, logs), 640, 480);
        stage.setTitle("Papaya Client");
        stage.setScene(scene);
        stage.show();
    }

    private Button generateStatusButton(Stage stage) {
        return new CreateDirectoryChooserButton().create("Status", stage, file -> {
            Optional<Path> maybePath = fileManager.generateStatus(file);
            maybePath.ifPresentOrElse(path -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Papaya File Status Created");
                alert.setHeaderText(path.toAbsolutePath().toString());
                alert.show();
            }, () -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error generating status");
                alert.setHeaderText(file.getName() + " error");
                alert.show();
            });
        });
    }

    private Button generateJoinButton(Stage stage) {
        return new CreateDirectoryChooserButton().create("Join", stage, file -> {
            Optional<Path> maybePath = fileManager.joinStore(file);
            maybePath.ifPresentOrElse(path -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Papaya File Joined");
                alert.setHeaderText(path.toAbsolutePath().toString());
                alert.show();
            }, () -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Papaya File Incomplete");
                alert.setHeaderText(file.getName() + " incomplete");
                alert.show();
            });
        });
    }

    private Button generateCreatePapayaFileButton(Stage stage) {
        return new CreateFileChooserButton().
                create("Create", stage, selectedFile -> {
                    if (selectedFile != null) {
                        try {
                            Path path = fileManager.split(selectedFile);
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Papaya File Created");
                            alert.setHeaderText(path.toAbsolutePath().toString());
                            alert.show();
                        } catch (IOException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Papaya File Creation Failed");
                            alert.show();
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private Button generateSendButton(Stage stage) {
        return new CreateDirectoryChooserButton().create("Send", stage, file -> {
            Optional<PapayaFile> maybePapayaFile = fileManager.retrievePapayaFile(file);
            maybePapayaFile.ifPresent(papayaFile -> {
                peerConnectionManager.send(papayaFile);
            });
        });
    }

    @Override
    public void stop() {
        System.out.println("Shutting down");
        peerConnectionManager.stop();
        SpringApplication.exit(PapayaClientApplication.getContext(), () -> 0);
    }
}
