package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.model.ResourceServiceMessage;
import org.example.service.ResourceProcessorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ResourceController {

   private final ResourceProcessorService resourceProcessorService;

   @PostMapping(path = "/sendMetadata", consumes = "application/json", produces = "application/json")
   public void sendMetadata(@RequestBody ResourceServiceMessage model) {
      resourceProcessorService.sendMetadata(model);
   }

   @DeleteMapping(path = "/sendMetadata", consumes = "application/json")
   public void deleteMetadata(@RequestBody ResourceServiceMessage model) {
      resourceProcessorService.deleteMetadata(model);
   }

   @KafkaListener(id = "entityJSONListener",
         containerFactory = "jsonEntityConsumerFactory",
         topics = "resource-service.entityJson",
         groupId = "search.entity-json-consumer")
   public void handleMessage(String value) {
      ResourceServiceMessage messageObject = resourceProcessorService.getDTO(value);
      if (messageObject.getMethod() == RequestMethod.POST) {
         sendMetadata(messageObject);
      } else if (messageObject.getMethod() == RequestMethod.DELETE) {
         deleteMetadata(messageObject);
      }
      System.err.println(value);
   }

}
