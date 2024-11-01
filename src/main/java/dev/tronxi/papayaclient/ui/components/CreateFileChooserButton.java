package dev.tronxi.papayaclient.ui.components;

import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.function.Consumer;

public class CreateFileChooserButton {

    public Button create(String buttonName, Stage stage, Consumer<File> consumer) {
        Button button = new Button(buttonName);
        button.setOnMouseClicked(mouseEvent -> {
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                consumer.accept(selectedFile);
            }
        });
        return button;
    }
}
