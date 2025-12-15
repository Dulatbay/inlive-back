package ai.lab.inlive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "ai.lab.inlivefilemanager.client.api")
@EnableScheduling
public class InliveApplication {

    public static void main(String[] args) {
        SpringApplication.run(InliveApplication.class, args);
    }

}
