package com.example.storageservice.controller;

import com.example.storageservice.model.StorageObject;
import com.example.storageservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StorageController {

   @Autowired
   private StorageService storageService;

   @PostMapping(path = "/api/storages")
   @PreAuthorize("hasRole('ADMIN')")
   public Long createNewStorage(@RequestBody StorageObject storageObject) {
      return storageService.createNewStorage(storageObject);
   }

   @GetMapping(path = "/api/storages")
   @PreAuthorize("hasAnyRole('USER','ADMIN')")
   public List<StorageObject> getAllStorages() {
      return storageService.getAllStorages();
   }

   @DeleteMapping(path = "/api/storages")
   @PreAuthorize("hasRole('ADMIN')")
   public List<Long> deleteStorages(@RequestParam List<Long> id) {
      return storageService.deleteStorages(id);
   }

   @GetMapping(path = "/storages/type")
   public StorageObject getStorageByType(@RequestParam String storageType) {
      return storageService.getStorageByType(storageType);
   }

   @KafkaListener(id = "entityJSONListener",
         containerFactory = "jsonEntityConsumerFactory",
         topics = "storage-service.entityJson",
         groupId = "search.entity-json-consumer")
   public void handleMessage(String value) {
      StorageObject messageObject = storageService.getStorageObjectFromMessage(value);
      StorageObject storageByType = storageService.getStorageByType(messageObject.getStorageType());
      storageService.sendMessage(storageByType);
      System.err.println(value);
   }

}
