package dev.tronxi.papayaclient.ui.components;

import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatus;
import dev.tronxi.papayaclient.persistence.papayastatusfile.PapayaStatusFile;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class PapayaProgress {

    private ProgressBar progressBar;
    private double progress;
    private Label percentLabel;

    public HBox create(PapayaStatusFile papayaStatusFile) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(5));
        hbox.setSpacing(10.0);

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
        percentLabel = new Label(percentCalculated + "%");


        hbox.getChildren().addAll(fileNameLabel, progressBar, percentLabel);
        return hbox;
    }

    public Void refresh(PapayaStatusFile papayaStatusFile) {
        progress = calculateProgress(papayaStatusFile);
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            percentLabel.setText(calculatePercent(progress) + "%");
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
