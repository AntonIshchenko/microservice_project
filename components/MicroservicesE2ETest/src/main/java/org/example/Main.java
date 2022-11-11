package org.example;

import org.example.tests.E2ETestLauncher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

public class Main {
   public static void main(String[] args) throws IOException {

      AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
      context.register(ApplicationConfig.class);
      context.refresh();
      E2ETestLauncher bean = context.getBean(E2ETestLauncher.class);
      try {
         bean.runTest();
      } catch (Exception e){
         System.err.println("Test failed!");
      }
      System.out.println("test completed!");

   }
}