package org.example.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.model.SongMetadataModel;
import org.example.service.ResourceProcessorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ResourceController {

   private final ResourceProcessorService resourceProcessorService;

   @ApiResponses(
         value = {
               @ApiResponse(code = 200, message = "OK"),
               @ApiResponse(code = 400, message = "Validation error missing metadata"),
               @ApiResponse(code = 500, message = "Internal server error occurred.")
         })
   @PostMapping(path = "/sendMetadata", consumes = "application/json", produces = "application/json")
   public void sendMetadata(@RequestBody SongMetadataModel model) {
      resourceProcessorService.sendMessage(model);
   }

   @KafkaListener(id = "entityJSONListener",
         containerFactory = "jsonEntityConsumerFactory",
         topics = "resource-service.entityJson",
         groupId = "search.entity-json-consumer")
   public void handleMessage(String value) {
      resourceProcessorService.handleSongMetadata(value);
      System.err.println(value);
   }

}
