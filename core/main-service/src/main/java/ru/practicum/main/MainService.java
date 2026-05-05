package ru.practicum.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"ru.practicum.main", "ru.practicum.stats.client"})
public class MainService {
    public static void main(String[] args) {
        SpringApplication.run(MainService.class, args);
    }
}