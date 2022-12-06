package com.example.storageservice.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.example.storageservice.model.StorageObject;
import com.example.storageservice.model.StorageType;
import com.example.storageservice.repository.StorageServiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {

   private static final String STORAGE_REGION = Regions.US_EAST_1.getName();

   @Value("${s3.storage.url}")
   private String storageUrl;

   private AWSS3Service awsService;
   private final KafkaTemplate<String, String> kafkaTemplate;
   private final ObjectMapper objectMapper;

   @Autowired
   private final StorageServiceRepository storageServiceRepository;

   @PostConstruct
   public void createStorageObjects() {
      AmazonS3 amazonS3 = AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(storageUrl, STORAGE_REGION))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("awsAccessKey", "awsSecretKey")))
            .withPathStyleAccessEnabled(true)
            .build();
      awsService = new AWSS3Service(amazonS3);

      //creating a bucket
//      deleteAllBuckets(); /// for test

      StorageObject defaultStagingStorage = new StorageObject(0, StorageType.STAGING.name(), "staging-bucket", "staging/staging-bucket");
      StorageObject defaultPermanentStorage = new StorageObject(0, StorageType.PERMANENT.name(), "permanent-bucket", "permanent/permanent-bucket");

      createNewStorage(defaultStagingStorage);
      createNewStorage(defaultPermanentStorage);
   }

   public Long createNewStorage(StorageObject storageObject) {
      if (!awsService.doesBucketExist(storageObject.getBucket())) {
         awsService.createBucket(storageObject.getBucket());
      }
      StorageObject saveResult = storageServiceRepository.save(storageObject);
      return saveResult.getId();
   }

   public List<StorageObject> getAllStorages() {
      return storageServiceRepository.findAll();
   }

   @Transactional
   public List<Long> deleteStorages(List<Long> id) {
      List<StorageObject> all = storageServiceRepository.findAll();
      all.forEach(e -> awsService.deleteBucket(e.getBucket()));
      id.forEach(storageServiceRepository::deleteStorageObjectById);
      return id;
   }

   public StorageObject getStorageByType(String storageType) {
      return storageServiceRepository.getStorageObjectByStorageType(storageType);
   }

   public StorageObject getStorageObjectFromMessage(String value) {
      try {
         return objectMapper.readValue(value, StorageObject.class);
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return new StorageObject();
      }
   }

   @SneakyThrows
   public ListenableFuture<SendResult<String, String>> sendMessage(StorageObject modelDTO) {
      String messageKey = modelDTO.getClass().getSimpleName() + "|" + modelDTO.getBucket();
      String messageValue = objectMapper.writeValueAsString(modelDTO);
      return kafkaTemplate.send("resource-service.entityJson", messageKey, messageValue);
   }

   @Transactional
   private void deleteAllBuckets() {
      List<Bucket> buckets = awsService.listBuckets();
      buckets.forEach(e -> awsService.deleteBucket(e.getName()));
      storageServiceRepository.deleteAll();
   }
}
