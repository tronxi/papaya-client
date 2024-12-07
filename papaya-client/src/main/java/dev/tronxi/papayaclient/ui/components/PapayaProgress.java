package dev.tronxi.papayaclient.ui.components;

import dev.tronxi.papayaclient.persistence.FileManager;
import dev.tronxi.papayaclient.persistence.papayastatusfile.JoinStatus;
import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatus;
import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatusFile;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.File;

public class PapayaProgress {

    private ProgressBar progressBar;
    private double progress;
    private Label percentLabel;
    private Button removeFolderButton;
    private Button openFolderButton;

    public HBox create(HostServices hostServices, FileManager fileManager, PapayaStatusFile papayaStatusFile) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10));
        hbox.setSpacing(10.0);
        hbox.setAlignment(Pos.CENTER_LEFT);

        Label fileNameLabel = new Label(papayaStatusFile.getFileName());
        Tooltip tooltip = new Tooltip(papayaStatusFile.getFileName());
        Tooltip.install(fileNameLabel, tooltip);
        fileNameLabel.setMinWidth(300);
        fileNameLabel.setMaxWidth(300);
        progressBar = new ProgressBar();
        progressBar.setMinWidth(150);
        progressBar.setMaxWidth(150);
        refresh(papayaStatusFile);
        String percentCalculated = calculatePercent(progress);
        percentLabel = new Label(percentCalculated);

        openFolderButton = getOpenFolderButton(hostServices, fileManager.getPapayaFolder(papayaStatusFile));
        removeFolderButton = getRemoveFolderButton(fileManager, papayaStatusFile);
        hbox.getChildren().addAll(fileNameLabel, progressBar, percentLabel, openFolderButton, removeFolderButton);
        return hbox;
    }

    private Button getRemoveFolderButton(FileManager fileManager, PapayaStatusFile papayaStatusFile) {
        Button removeFolderButton = new Button("\uD83D\uDDD1");
        removeFolderButton.managedProperty().bind(removeFolderButton.visibleProperty());
        removeFolderButton.setStyle("""
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-padding: 4 8;\s
                -fx-background-color: white;\s
                -fx-text-fill: #f61313;\s
                -fx-border-color: #cccccc;\s
                -fx-border-width: 1;\s
                -fx-border-radius: 3;
                -fx-cursor: hand;
                """);
        removeFolderButton.setOnMouseClicked(mouseEvent -> showConfirmationDialog(fileManager, papayaStatusFile));
        return removeFolderButton;
    }

    private void showConfirmationDialog(FileManager fileManager, PapayaStatusFile papayaStatusFile) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Are you sure you want to delete?");
        alert.setContentText("This will delete the folder and all its associated files.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                fileManager.removePapayaFolder(papayaStatusFile);
            }
        });
    }

    private Button getOpenFolderButton(HostServices hostServices, File papayaFolder) {
        Button openFolderButton = new Button("\uD83D\uDCC1");
        openFolderButton.managedProperty().bind(openFolderButton.visibleProperty());
        openFolderButton.setStyle("""
                -fx-font-size: 16px;
                -fx-padding: 4 8;\s
                -fx-background-color: white;\s
                -fx-text-fill: #333333;\s
                -fx-border-color: #cccccc;\s
                -fx-border-width: 1;\s
                -fx-border-radius: 3;
                -fx-cursor: hand;
                """);
        openFolderButton.setOnMouseClicked(mouseEvent -> {
            if (papayaFolder.exists()) {
                hostServices.showDocument(papayaFolder.toURI().toString());
            }
        });
        return openFolderButton;
    }

    public Void refresh(PapayaStatusFile papayaStatusFile) {
        progress = calculateProgress(papayaStatusFile);
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            percentLabel.setText(calculatePercent(progress));
            if (papayaStatusFile.getStatus().equals(PapayaStatus.COMPLETE)) {
                if (papayaStatusFile.getJoinStatus().equals(JoinStatus.STARTED)) {
                    progressBar.setStyle("-color-progress-bar-fill: #f5dc3c");
                } else if (papayaStatusFile.getJoinStatus().equals(JoinStatus.COMPLETED)) {
                    progressBar.setStyle("-color-progress-bar-fill: #5ac456");
                }
                openFolderButton.setVisible(true);
                removeFolderButton.setVisible(true);
            } else {
                progressBar.setStyle("-color-progress-bar-fill: #0ca3ff");
                openFolderButton.setVisible(false);
                removeFolderButton.setVisible(false);
            }
        });
        return null;
    }

    private double calculateProgress(PapayaStatusFile papayaStatusFile) {
        long completeCount = papayaStatusFile.getPartStatusFiles().stream()
                .filter(part -> part.getStatus() == PapayaStatus.COMPLETE)
                .count();
        return (double) completeCount / papayaStatusFile.getPartStatusFiles().size();
    }

    private String calculatePercent(double progress) {
        double percent = progress * 100;
        return String.format("%.2f%%", percent);
    }
}
