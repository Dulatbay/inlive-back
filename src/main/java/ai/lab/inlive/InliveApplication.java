package ai.lab.inlive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ai.lab.inlivefilemanager.client.api")
public class InliveApplication {

    public static void main(String[] args) {
        SpringApplication.run(InliveApplication.class, args);
    }

}
