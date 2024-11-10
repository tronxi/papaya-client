package dev.tronxi.papayaclient.ui.components;

import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.function.Consumer;

public class CreateDirectoryChooserButton {
    public Button create(String buttonName, Stage stage, Consumer<File> consumer) {
        Button button = new Button(buttonName);
        button.setOnMouseClicked(mouseEvent -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(stage);
            if (selectedDirectory != null) {
                consumer.accept(selectedDirectory);
            }
        });
        return button;
    }
}
