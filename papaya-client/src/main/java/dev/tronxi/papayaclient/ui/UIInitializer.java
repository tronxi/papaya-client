package dev.tronxi.papayaclient.ui;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import dev.tronxi.papayaclient.PapayaClientApplication;
import dev.tronxi.papayaclient.persistence.FileManager;
import dev.tronxi.papayaclient.persistence.papayafile.PapayaFile;
import dev.tronxi.papayaclient.peer.PeerConnectionManager;
import dev.tronxi.papayaclient.peer.PeerConnectionManagerTCP;
import dev.tronxi.papayaclient.persistence.services.ConfigService;
import dev.tronxi.papayaclient.ui.components.ConfigView;
import dev.tronxi.papayaclient.ui.components.CreateFileChooserButton;
import dev.tronxi.papayaclient.ui.components.PapayaProgress;
import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Optional;

@Service
public class UIInitializer extends Application {

    private FileManager fileManager;
    private PeerConnectionManager peerConnectionManager;
    private ConfigService configService;

    @Override
    public void init() {
        fileManager = PapayaClientApplication.getContext().getBean(FileManager.class);
        peerConnectionManager = PapayaClientApplication.getContext().getBean(PeerConnectionManagerTCP.class);
        configService = PapayaClientApplication.getContext().getBean(ConfigService.class);
    }

    @Override
    public void start(Stage stage) {
        setColorScheme();
        Label createPapayaFileRunning = new Label("CreatePapayaFileRunning...");
        createPapayaFileRunning.managedProperty().bind(createPapayaFileRunning.visibleProperty());
        createPapayaFileRunning.setVisible(false);
        Button createPapayaFileButton = generateCreatePapayaFileButton(stage, createPapayaFileRunning);

        Button downloadButton = generateDownloadButton(stage);

        Button configButton = generateConfigbutton();

        HBox buttonsBox = new HBox(configButton, createPapayaFileButton, downloadButton, createPapayaFileRunning);
        buttonsBox.setPadding(new Insets(10));
        buttonsBox.setSpacing(10);

        TextArea logs = new TextArea();
        logs.setMaxHeight(500);
        logs.setEditable(false);
        logs.setText("");

        peerConnectionManager.start(logs);
        new Thread(() -> fileManager.startJoinStarted()).start();

        VBox papayaProgressVBox = new VBox();
        fileManager.setNewPapayaStatusFileFunction((papayaStatusFile -> {
            Platform.runLater(() -> {
                PapayaProgress papayaProgress = new PapayaProgress();
                fileManager.addUpdateFunction(papayaStatusFile.getFileId(), papayaProgress::refresh);
                papayaProgressVBox.getChildren().add(papayaProgress.create(getHostServices(), fileManager, papayaStatusFile));
            });
            return null;
        }));
        fileManager.addDeletedPapayaStatusFileFunction((papayaStatusFile) -> {
            retrieveAllPapayaStatus(papayaProgressVBox);
            return null;
        });
        retrieveAllPapayaStatus(papayaProgressVBox);
        ScrollPane progressScrollPane = new ScrollPane(papayaProgressVBox);
        progressScrollPane.setFitToWidth(true);

        ScrollPane logsScrollPane = new ScrollPane(logs);
        logsScrollPane.setFitToWidth(true);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPane.getItems().addAll(progressScrollPane, logs);
        splitPane.setDividerPositions(0.8);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(buttonsBox);
        borderPane.setCenter(splitPane);

        Scene scene = new Scene(borderPane, 900, 580);
        stage.setTitle("Papaya");
        stage.setScene(scene);
        stage.show();
    }

    private void setColorScheme() {
        Platform.Preferences preferences = Platform.getPreferences();
        ColorScheme colorScheme = preferences.getColorScheme();
        if (ColorScheme.DARK.equals(colorScheme)) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }
        preferences.colorSchemeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == ColorScheme.DARK) {
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            } else {
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            }
        });
    }

    private Button generateConfigbutton() {
        Button configButton = new Button("⚙ Config");
        configButton.setOnMouseClicked(event -> {
            new ConfigView().render(configService);
        });
        return configButton;
    }

    private void retrieveAllPapayaStatus(VBox papayaProgressVBox) {
        Platform.runLater(() -> {
            papayaProgressVBox.getChildren().clear();
            fileManager.findAll().forEach(papayaStatusFile -> {
                PapayaProgress papayaProgress = new PapayaProgress();
                fileManager.addUpdateFunction(papayaStatusFile.getFileId(), papayaProgress::refresh);
                papayaProgressVBox.getChildren().add(papayaProgress.create(getHostServices(), fileManager, papayaStatusFile));
            });
        });
    }


    private Button generateDownloadButton(Stage stage) {
        return new CreateFileChooserButton().create("⬇ Download", stage, file -> {
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

    private Button generateCreatePapayaFileButton(Stage stage, Label label) {
        return new CreateFileChooserButton().
                create("➕ Create", stage, selectedFile -> {
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
