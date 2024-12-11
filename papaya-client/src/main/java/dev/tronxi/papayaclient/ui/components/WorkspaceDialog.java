package dev.tronxi.papayaclient.ui.components;

import dev.tronxi.papayaclient.persistence.services.ConfigService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

public class WorkspaceDialog {

    private File workspace;

    public void createWorkspaceDialog(Stage stage, ConfigService configService) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Papaya - Workspace Not Found");
        alert.setHeaderText("Workspace required to continue");

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        Label message = new Label("Please select a workspace and restart the application.");
        message.setWrapText(true);

        Label workspaceLabel = new Label("");

        Button saveButton = new Button("Save");
        saveButton.setDisable(true);

        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();

        Button workspaceButton = new FolderChooseButton().create("Workspace", stage, folder -> {
            alertStage.setAlwaysOnTop(true);
            if (folder != null && folder.exists() && folder.isDirectory()) {
                workspace = folder;
                workspaceLabel.setText(folder.getAbsolutePath());
                saveButton.setDisable(false);
            }
        });

        HBox workspaceBox = new HBox(10);
        workspaceBox.getChildren().addAll(workspaceButton, workspaceLabel);
        workspaceBox.setAlignment(Pos.CENTER_LEFT);
        workspaceBox.setFillHeight(false);

        saveButton.setOnAction(event -> {
            if (workspace != null) {
                configService.saveWorkspace(workspace.getAbsolutePath());
                alertStage.close();
                stage.close();
            }
        });

        content.getChildren().addAll(message, workspaceBox, saveButton);

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
}
