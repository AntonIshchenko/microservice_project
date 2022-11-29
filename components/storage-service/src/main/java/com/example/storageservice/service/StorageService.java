package com.example.storageservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class StorageService {

   private AWSS3Service awsService;
   private final KafkaTemplate<String, String> kafkaTemplate;
   private final ObjectMapper objectMapper;

   @PostConstruct
   public void createStorageObjects() {

   }

}
