package com.example.userservice;

import com.example.userservice.configuration.ApplicationConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
@Import(ApplicationConfiguration.class)
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(UserServiceApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.REACTIVE);
        springApplication.run(args);
    }

}
