package com.example.storageservice.controller;

import com.example.storageservice.model.StorageObject;
import com.example.storageservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StorageController {

   @Autowired
   private StorageService storageService;

   @PostMapping(path = "/storages")
   public Long createNewStorage(StorageObject storageObject) {
      return 0L;
   }

   @GetMapping(path = "/storages")
   public List<StorageObject> getAllStorages() {
      return Collections.singletonList(new StorageObject());
   }

   @DeleteMapping(path = "/storages")
   public List<Long> deleteStorages(@RequestParam List<Long> id) {
      return Collections.singletonList(1L);
   }

}
