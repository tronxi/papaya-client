package dev.tronxi.papayaclient.ui;

import dev.tronxi.papayaclient.PapayaClientApplication;
import dev.tronxi.papayaclient.persistence.FileManager;
import dev.tronxi.papayaclient.persistence.papayafile.PapayaFile;
import dev.tronxi.papayaclient.peer.PeerConnectionManager;
import dev.tronxi.papayaclient.peer.PeerConnectionManagerTCP;
import dev.tronxi.papayaclient.ui.components.CreateDirectoryChooserButton;
import dev.tronxi.papayaclient.ui.components.CreateFileChooserButton;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
        Label createPapayaFileRunning = new Label("CreatePapayaFileRunning...");
        createPapayaFileRunning.managedProperty().bind(createPapayaFileRunning.visibleProperty());
        createPapayaFileRunning.setVisible(false);
        Button createPapayaFileButton = generateCreatePapayaFileButton(stage, createPapayaFileRunning);

        Label joinRunning = new Label("JoinRunning...");
        joinRunning.managedProperty().bind(joinRunning.visibleProperty());
        joinRunning.setVisible(false);
        Button joinButton = generateJoinButton(stage, joinRunning);

        Label statusRunning = new Label("StatusRunning...");
        statusRunning.managedProperty().bind(statusRunning.visibleProperty());
        statusRunning.setVisible(false);
        Button statusButton = generateStatusButton(stage, statusRunning);

        Button downloadButton = generateDownloadButton(stage);

        HBox hBox = new HBox(createPapayaFileButton, joinButton, statusButton, downloadButton,
                createPapayaFileRunning, joinRunning, statusRunning);
        hBox.setSpacing(10);

        TextArea logs = new TextArea();
        logs.setEditable(false);
        logs.setText("");
        logs.setMaxHeight(400);
        logs.setMinHeight(400);
        peerConnectionManager.start(logs);
        peerConnectionManager.startAllIncompleteDownloads();

        VBox vBox = new VBox(hBox, logs);
        vBox.setSpacing(10);
        Scene scene = new Scene(vBox, 800, 480);
        stage.setTitle("Papaya");
        stage.setScene(scene);
        stage.show();
    }

    private Button generateDownloadButton(Stage stage) {
        return new CreateFileChooserButton().create("Download", stage, file -> {
            Optional<PapayaFile> maybePapayaFile = fileManager.retrievePapayaFileFromFile(file);
            maybePapayaFile.ifPresent(papayaFile -> {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        peerConnectionManager.download(papayaFile);
                        return null;
                    }
                };
                new Thread(task).start();
            });
        });
    }

    private Button generateStatusButton(Stage stage, Label label) {
        return new CreateDirectoryChooserButton().create("Status", stage, file -> {
            Task<Optional<Path>> task = new Task<>() {
                @Override
                protected Optional<Path> call() {
                    return fileManager.generateStatus(file);
                }
            };
            task.setOnRunning(workerStateEvent -> {
                label.setVisible(true);
            });
            task.setOnSucceeded(workerStateEvent -> {
                label.setVisible(false);
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

    private Button generateJoinButton(Stage stage, Label label) {
        return new CreateDirectoryChooserButton().create("Join", stage, file -> {
            Task<Optional<Path>> task = new Task<>() {
                @Override
                protected Optional<Path> call() {
                    return fileManager.joinStore(file);
                }
            };
            task.setOnRunning(workerStateEvent -> {
                label.setVisible(true);
            });
            task.setOnSucceeded(workerStateEvent -> {
                label.setVisible(false);
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

    private Button generateCreatePapayaFileButton(Stage stage, Label label) {
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
                            label.setVisible(true);
                        });
                        task.setOnSucceeded(workerStateEvent -> {
                            label.setVisible(false);
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

    @Override
    public void stop() {
        peerConnectionManager.stop();
        SpringApplication.exit(PapayaClientApplication.getContext(), () -> 0);
    }
}
