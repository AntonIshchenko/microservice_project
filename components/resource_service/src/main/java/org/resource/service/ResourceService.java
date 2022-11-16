package org.resource.service;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.resource.model.BinaryResourceModel;
import org.resource.repository.UploadedContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ResourceService {

   private static final String STORAGE_REGION = Regions.US_EAST_1.getName();

   @Value("${s3.storage.url}")
   private String storageUrl;
   @Value("${s3.storage.bucket.name}")
   private String bucketName;

   private AWSS3Service awsService;
   private final KafkaTemplate<String, String> kafkaTemplate;
   private final ObjectMapper objectMapper;

   @Autowired
   private UploadedContentRepository uploadedContentRepository;

   @PostConstruct
   private void initializeAWSS3Storage() {
      AmazonS3 amazonS3 = AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(storageUrl, STORAGE_REGION))
            .withPathStyleAccessEnabled(true)
            .build();
      awsService = new AWSS3Service(amazonS3);

      //      clearBucket(); // remove comment to clear bucket
      //creating a bucket
      if (!awsService.doesBucketExist(bucketName)) {
         awsService.createBucket(bucketName);
      }
   }

   @SneakyThrows
   @Transactional
   public Long uploadNewResource(MultipartFile data) {

      BinaryResourceModel resourceModel = new BinaryResourceModel(0, data.getOriginalFilename(), RequestMethod.POST);
      BinaryResourceModel ifExist = uploadedContentRepository.getBinaryResourceModelByName(data.getOriginalFilename());
      if (ifExist != null) {
         resourceModel.setResourceId(ifExist.getResourceId());
         sendMessage(resourceModel);
         return ifExist.getResourceId();
      }

      BinaryResourceModel model = uploadedContentRepository.save(resourceModel);

      if (!uploadDataToBucket(data.getOriginalFilename(), data.getInputStream())) {
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error occurred.");
      }
      sendMessage(model);
      return model.getResourceId();
   }

   public S3ObjectInputStream getAudioBinaryData(Long id) {
      try {
         BinaryResourceModel model = uploadedContentRepository.getBinaryResourceModelByResourceId(id);
         if (model == null)
            throw new IOException();
         S3ObjectInputStream dataFromBucket = getDataFromBucket(model);
         model.setMethod(RequestMethod.GET);
         sendMessage(model);
         return dataFromBucket;
      } catch (EntityNotFoundException | IOException e) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource doesn’t exist with given id");
      }
   }

   @Transactional
   public List<Long> deleteSongs(List<Long> id) {
      for (Long e : id) {
         BinaryResourceModel model = uploadedContentRepository.getBinaryResourceModelByResourceId(e);
         uploadedContentRepository.deleteBinaryResourceModelByResourceId(model.getResourceId());
         deleteDataFromBucket(model.getName());
         model.setMethod(RequestMethod.DELETE);
         sendMessage(model);
      }

      return id;
   }

   public ResponseEntity<byte[]> getAudioBinaryDataWithRange(Long id, List<Integer> range) {
      if (range.size() != 2)
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid range");

      int length = range.get(1) - range.get(0);
      if (length < 0)
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid range");

      byte[] result = new byte[length];
      S3ObjectInputStream audioBinaryData = getAudioBinaryData(id);

      try {
         audioBinaryData.skip(range.get(0));
         audioBinaryData.read(result, 0, length);
      } catch (IOException e) {
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid range");
      }

      return new ResponseEntity<>(result, HttpStatus.PARTIAL_CONTENT);
   }

   private boolean uploadDataToBucket(String key, InputStream data) {
      List<String> keys = new ArrayList<>();

      awsService.listObjects(bucketName).getObjectSummaries().forEach(e -> keys.add(e.getKey()));
      if (keys.contains(key)) {
         return true;
      }
      PutObjectResult putObjectResult = awsService.putObject(bucketName, key, data);
      return putObjectResult.getETag() != null;
   }

   private S3ObjectInputStream getDataFromBucket(BinaryResourceModel resourceModel) {
      S3Object s3Object = awsService.getObject(bucketName, resourceModel.getName());
      return s3Object.getObjectContent();
   }

   private void deleteDataFromBucket(String key) {
      awsService.deleteObject(bucketName, key);
   }

   @SneakyThrows
   private void sendMessage(BinaryResourceModel model) {
      String messageKey = model.getClass().getSimpleName() + "|" + model.getName();
      String messageValue = objectMapper.writeValueAsString(model);
      kafkaTemplate.send("resource-service.entityJson", messageKey, messageValue);
   }

   private void clearBucket() {
      List<String> keys = new ArrayList<>();
      awsService.listObjects(bucketName).getObjectSummaries().forEach(e -> keys.add(e.getKey()));
      keys.forEach(e -> awsService.deleteObject(bucketName, e));
   }

}
