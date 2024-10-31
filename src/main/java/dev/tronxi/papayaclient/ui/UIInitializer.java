package dev.tronxi.papayaclient.ui;

import dev.tronxi.papayaclient.PapayaClientApplication;
import dev.tronxi.papayaclient.files.FileManager;
import dev.tronxi.papayaclient.udp.UdpClient;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UIInitializer extends Application {

    private FileManager fileManager;
    private UdpClient udpClient;

    @Override
    public void init() {
        fileManager = PapayaClientApplication.getContext().getBean(FileManager.class);
        udpClient = PapayaClientApplication.getContext().getBean(UdpClient.class);
    }

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Button splitButton = new Button("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        splitButton.setOnMouseClicked(mouseEvent -> {
            try {
                fileManager.split("p2p.webp");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        TextArea logs = new TextArea();
        logs.setEditable(false);
        logs.setText("");
        logs.setMaxHeight(100);
        udpClient.start(logs);
        Scene scene = new Scene(new VBox(splitButton, logs), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        System.out.println("Shutting down");
        udpClient.stop();
        SpringApplication.exit(PapayaClientApplication.getContext(), () -> 0);
    }
}
