package dev.tronxi.papayaclient;

import dev.tronxi.papayaclient.ui.UIInitializer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PapayaClientApplication {

    //private static ConfigurableApplicationContext context;

//    public static void main(String[] args) {
//        //context = SpringApplication.run(PapayaClientApplication.class, args);
//        Application.launch(UIInitializer.class, args);
//    }

//    public static ConfigurableApplicationContext getContext() {
//        return context;
//    }

    //@Override
//    public void start(Stage stage) throws Exception {
//        BorderPane borderPane = new BorderPane();
//
//        Scene scene = new Scene(borderPane, 900, 580);
//        stage.setTitle("Papaya");
//        stage.setScene(scene);
//        stage.show();
//
//    }
}
