package dev.tronxi.papayaclient.ui.components;

import dev.tronxi.papayaclient.persistence.services.ConfigService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfigView {

    public void render(ConfigService configService) {
        Stage stage = new Stage();

        TextField workspaceTextField = new TextField(configService.retrieveWorkspace().orElse(""));
        HBox workspaceProperty = createPropertyRow("Workspace:", workspaceTextField);

        TextField trackerTextField = new TextField(configService.retrieveTracker());
        HBox trackerProperty = createPropertyRow("Tracker:", trackerTextField);

        CheckBox useOnlyLocalAddressCheckBox = new CheckBox("");
        useOnlyLocalAddressCheckBox.setSelected(configService.retrieveUseOnlyLocalAddress());
        HBox useOnlyLocalAddressProperty = createPropertyRow("UseOnlyLocalAddress:", useOnlyLocalAddressCheckBox);

        Button saveButton = createSaveButton(stage, configService, workspaceTextField, trackerTextField, useOnlyLocalAddressCheckBox);

        VBox mainLayout = new VBox(workspaceProperty, trackerProperty, useOnlyLocalAddressProperty, saveButton);
        mainLayout.setSpacing(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(mainLayout, 500, 300);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle("Configuration");
        stage.showAndWait();
    }

    private Button createSaveButton(Stage stage, ConfigService configService, TextField workspaceTextField, TextField trackerTextField, CheckBox useOnlyLocalAddressCheckBox) {
        Button saveButton = new Button("\uD83D\uDCBE Save");
        saveButton.getStyleClass().add("success");
        saveButton.setOnAction(event -> {
            boolean userConfirmed = showConfirmationDialog(stage);
            if (userConfirmed) {
                configService.saveWorkspace(workspaceTextField.getText());
                configService.saveTracker(trackerTextField.getText());
                configService.saveUseOnlyLocalAddress(useOnlyLocalAddressCheckBox.isSelected());
                stage.close();
            }
        });
        return saveButton;
    }

    private boolean showConfirmationDialog(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Configuration Changes");
        alert.setHeaderText("Save Configuration Changes?");
        alert.setContentText("Changes will take effect after restarting the application.");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(stage);
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }

    private HBox createPropertyRow(String labelText, Control control) {
        Label label = new Label(labelText);
        label.setMinWidth(100);
        control.setMinWidth(250);
        HBox propertyRow = new HBox(label, control);
        propertyRow.setSpacing(10);
        propertyRow.setAlignment(Pos.CENTER_LEFT);
        return propertyRow;
    }
}
