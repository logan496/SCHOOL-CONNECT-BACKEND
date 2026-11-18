package com.schoolconnect.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SchoolConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchoolConnectApplication.class, args);
    }
}
