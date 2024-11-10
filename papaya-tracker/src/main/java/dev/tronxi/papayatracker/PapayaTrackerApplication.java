package dev.tronxi.papayatracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PapayaTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PapayaTrackerApplication.class, args);
    }

}
