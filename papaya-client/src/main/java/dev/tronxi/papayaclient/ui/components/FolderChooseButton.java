package dev.tronxi.papayaclient.ui.components;

import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.function.Consumer;

public class FolderChooseButton {
    public Button create(String buttonName, Stage stage, Consumer<File> consumer) {
        Button button = new Button(buttonName);
        button.setOnMouseClicked(mouseEvent -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedFolder = directoryChooser.showDialog(stage);
            consumer.accept(selectedFolder);
        });
        return button;
    }
}
