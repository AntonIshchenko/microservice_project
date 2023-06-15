package org.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.resource.*")
public class ResourceServiceApplication {

   public static void main(String[] args) {
      SpringApplication.run(ResourceServiceApplication.class, args);
   }

}