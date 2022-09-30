package com.tgin.esiaauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({EsiaProperties.class})
public class EsiaAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsiaAuthServiceApplication.class, args);
    }

}
