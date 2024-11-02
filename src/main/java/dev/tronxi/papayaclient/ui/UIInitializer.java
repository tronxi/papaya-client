package dev.tronxi.papayaclient.ui;

import dev.tronxi.papayaclient.PapayaClientApplication;
import dev.tronxi.papayaclient.files.FileManager;
import dev.tronxi.papayaclient.files.papayafile.PapayaFile;
import dev.tronxi.papayaclient.peer.PeerConnectionManager;
import dev.tronxi.papayaclient.peer.PeerConnectionManagerTCP;
import dev.tronxi.papayaclient.peer.PeerConnectionManagerUDP;
import dev.tronxi.papayaclient.ui.components.CreateDirectoryChooserButton;
import dev.tronxi.papayaclient.ui.components.CreateFileChooserButton;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Optional;

@Service
public class UIInitializer extends Application {

    private FileManager fileManager;
    private PeerConnectionManager peerConnectionManager;

    @Override
    public void init() {
        fileManager = PapayaClientApplication.getContext().getBean(FileManager.class);
        peerConnectionManager = PapayaClientApplication.getContext().getBean(PeerConnectionManagerTCP.class);
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
            Task<Optional<Path>> task = new Task<>() {
                @Override
                protected Optional<Path> call() {
                    return fileManager.generateStatus(file);
                }
            };
            task.setOnSucceeded(workerStateEvent -> {
                Optional<Path> maybePath = task.getValue();
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
            new Thread(task).start();
        });
    }

    private Button generateJoinButton(Stage stage) {
        return new CreateDirectoryChooserButton().create("Join", stage, file -> {
            Task<Optional<Path>> task = new Task<>() {
                @Override
                protected Optional<Path> call() {
                    System.out.println("joining");
                    return fileManager.joinStore(file);
                }
            };
            task.setOnSucceeded(workerStateEvent -> {
                System.out.println("joined");
                Optional<Path> maybePath = task.getValue();
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
            new Thread(task).start();
        });
    }

    private Button generateCreatePapayaFileButton(Stage stage) {
        return new CreateFileChooserButton().
                create("Create", stage, selectedFile -> {
                    if (selectedFile != null) {
                        Task<Optional<Path>> task = new Task<>() {
                            @Override
                            protected Optional<Path> call() {
                                return fileManager.split(selectedFile);
                            }
                        };
                        task.setOnRunning(workerStateEvent -> {
                            System.out.println("Running task");
                        });
                        task.setOnSucceeded(workerStateEvent -> {
                            Optional<Path> maybePath = task.getValue();
                            Alert alert;
                            if (maybePath.isPresent()) {
                                alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Papaya File Created");
                                alert.setHeaderText(maybePath.get().toAbsolutePath().toString());
                            } else {
                                alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Papaya File Creation Failed");
                            }
                            alert.show();
                        });
                        new Thread(task).start();
                    }
                });
    }

    private Button generateSendButton(Stage stage) {
        return new CreateDirectoryChooserButton().create("Send", stage, file -> {
            System.out.println("retrieving file");
            Optional<PapayaFile> maybePapayaFile = fileManager.retrievePapayaFile(file);
            System.out.println("Tenemos File");
            maybePapayaFile.ifPresent(papayaFile -> {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        peerConnectionManager.send(papayaFile);
                        return null;
                    }
                };
                task.setOnSucceeded(workerStateEvent -> {
                    System.out.println("Sent file");
                });
                new Thread(task).start();
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
