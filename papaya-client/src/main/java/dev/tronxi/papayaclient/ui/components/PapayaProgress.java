package dev.tronxi.papayaclient.ui.components;

import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatus;
import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatusFile;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

import java.io.File;
import java.util.logging.Logger;

public class PapayaProgress {

    private static final Logger logger = Logger.getLogger(PapayaProgress.class.getName());

    private ProgressBar progressBar;
    private double progress;
    private Label percentLabel;


    public HBox create(HostServices hostServices, File papayaFolder, PapayaStatusFile papayaStatusFile) {
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

        Button openFolderButton = getOpenFolderButton(hostServices, papayaFolder);
        hbox.getChildren().addAll(fileNameLabel, progressBar, percentLabel, openFolderButton);
        return hbox;
    }

    private Button getOpenFolderButton(HostServices hostServices, File papayaFolder) {
        Button openFolderButton = new Button("\uD83D\uDCC1");
        openFolderButton.setStyle("""
                    -fx-font-size: 16px;
                    -fx-font-weight: bold;
                    -fx-padding: 5 10 5 10; 
                    -fx-background-color: #0078D7; 
                    -fx-text-fill: white;
                    -fx-background-radius: 5; 
                    -fx-border-radius: 5; 
                    -fx-border-color: #005A9E;
                    -fx-border-width: 1;
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
                progressBar.setStyle("-fx-accent: #5ac456;");
            } else {
                progressBar.setStyle("-fx-accent: #0ca3ff;");
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
