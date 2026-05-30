package cn.jxufe.seedsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SeedsystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeedsystemApplication.class, args);
    }

}
