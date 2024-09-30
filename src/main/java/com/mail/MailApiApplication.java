package com.mail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MailApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(MailApiApplication.class, args);
  }
}
