package com.kt.cdp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CriminalDetentionPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(CriminalDetentionPortalApplication.class, args);
    }
}
