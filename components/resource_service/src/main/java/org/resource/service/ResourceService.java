package org.resource.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.resource.model.BinaryResourceModel;
import org.resource.model.SongMetadataModel;
import org.resource.model.StorageObject;
import org.resource.model.StorageType;
import org.resource.repository.UploadedContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Service
public class ResourceService {

   private static final String STORAGE_REGION = Regions.US_EAST_1.getName();

   @Value("${s3.storage.url}")
   private String storageUrl;
   @Value("${storage.service.url}")
   private String storageServiceUrl;

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
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("awsAccessKey", "awsSecretKey")))
            .withPathStyleAccessEnabled(true)
            .build();
      awsService = new AWSS3Service(amazonS3);
   }

   public String echo() {
      String result = storageUrl + "\n" + "\n";
      List<Bucket> buckets = awsService.listBuckets();
      for (Bucket bucket : buckets) {
         result += bucket.getName() + "\n";
      }
      ListenableFuture<SendResult<String, String>> sendResultListenableFuture = sendMessage(new BinaryResourceModel());
      try {
         result += sendResultListenableFuture.get().getProducerRecord().value();
      } catch (InterruptedException | ExecutionException e) {
         System.err.println(e.getMessage());
      }

      return result;
   }

   @SneakyThrows
   @Transactional
   public Long uploadNewResource(MultipartFile data) {

      long resourceId = System.currentTimeMillis();

      BinaryResourceModel resourceModel = new BinaryResourceModel(0, resourceId, data.getOriginalFilename(), StorageType.STAGING.name(), RequestMethod.POST);
      BinaryResourceModel ifExist = uploadedContentRepository.getBinaryResourceModelByName(data.getOriginalFilename());
      if (ifExist != null && !Objects.equals(ifExist.getStorageType(), StorageType.STAGING.name())) {
         resourceModel.setResourceId(ifExist.getResourceId());
         sendMessage(resourceModel);
         return ifExist.getResourceId();
      }

      BinaryResourceModel model = uploadedContentRepository.save(resourceModel);

      if (!uploadDataToBucket(data.getOriginalFilename(), data.getInputStream(), StorageType.STAGING)) {
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error occurred.");
      }
      sendMessage(model);
      return model.getResourceId();
   }

   public S3ObjectInputStream getAudioBinaryData(Long id, StorageType storageType) {
      StorageObject storage = getStorageByType(storageType);
      try {
         BinaryResourceModel model = uploadedContentRepository.getBinaryResourceModelByResourceId(id);
         if (model == null)
            throw new IOException();
         S3ObjectInputStream dataFromBucket = getDataFromBucket(model, storage);
         model.setMethod(RequestMethod.GET);
         sendMessage(model);
         return dataFromBucket;
      } catch (EntityNotFoundException | IOException e) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource doesn’t exist with given id");
      }
   }

   public BinaryResourceModel getResourceModelByID(Long id) {
      return uploadedContentRepository.getBinaryResourceModelByResourceId(id);
   }

   @Transactional
   public List<Long> deleteSongs(List<Long> id, StorageType storageType) {
      StorageObject storage = getStorageByType(storageType);
      for (Long e : id) {
         BinaryResourceModel model = uploadedContentRepository.getBinaryResourceModelByResourceId(e);
         uploadedContentRepository.deleteBinaryResourceModelByResourceId(model.getResourceId());
         deleteDataFromBucket(model.getName(), storage);
         model.setMethod(RequestMethod.DELETE);
         sendMessage(model);
      }

      return id;
   }

   public ResponseEntity<byte[]> getAudioBinaryDataWithRange(Long id, List<Integer> range, StorageType storageType) {
      if (range.size() != 2)
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid range");

      int length = range.get(1) - range.get(0);
      if (length < 0)
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid range");

      byte[] result = new byte[length];
      S3ObjectInputStream audioBinaryData = getAudioBinaryData(id, storageType);

      try {
         audioBinaryData.skip(range.get(0));
         audioBinaryData.read(result, 0, length);
      } catch (IOException e) {
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid range");
      }

      return new ResponseEntity<>(result, HttpStatus.PARTIAL_CONTENT);
   }

   private boolean uploadDataToBucket(String key, InputStream data, StorageType storageType) {
      List<String> keys = new ArrayList<>();

      StorageObject storage = getStorageByType(storageType);

      awsService.listObjects(storage.getBucket()).getObjectSummaries().forEach(e -> keys.add(e.getKey()));
      if (keys.contains(key)) {
         return true;
      }
      PutObjectResult putObjectResult = awsService.putObject(storage.getBucket(), key, data);
      return putObjectResult.getETag() != null;
   }

   private S3ObjectInputStream getDataFromBucket(BinaryResourceModel resourceModel, StorageObject storageObject) {
      S3Object s3Object = awsService.getObject(storageObject.getBucket(), resourceModel.getName());
      return s3Object.getObjectContent();
   }

   private void deleteDataFromBucket(String key, StorageObject storageObject) {
      awsService.deleteObject(storageObject.getBucket(), key);
   }

   @SneakyThrows
   private ListenableFuture<SendResult<String, String>> sendMessage(BinaryResourceModel model) {
      String messageKey = model.getClass().getSimpleName() + "|" + model.getName();
      String messageValue = objectMapper.writeValueAsString(model);
      return kafkaTemplate.send("resource-service.entityJson", messageKey, messageValue);
   }

   @SneakyThrows
   private StorageObject getStorageByType(StorageType storageType) {
      HttpUriRequest postRequest = RequestBuilder.get()
            .setUri(storageServiceUrl)
            .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .addParameter("storageType", storageType.name())
            .build();
      HttpResponse postResponse = HttpClientBuilder.create().build().execute(postRequest);

      String response = new String(postResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
      StorageObject storageObject = objectMapper.readValue(response, StorageObject.class);
      return storageObject;
   }

   private void clearBucket(StorageObject storageObject) {
      List<String> keys = new ArrayList<>();
      awsService.listObjects(storageObject.getBucket()).getObjectSummaries().forEach(e -> keys.add(e.getKey()));
      keys.forEach(e -> awsService.deleteObject(storageObject.getBucket(), e));
   }

   public SongMetadataModel getDTO(String inputJson) {
      try {
         return objectMapper.readValue(inputJson, SongMetadataModel.class);
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return new SongMetadataModel();
      }
   }

   @Transactional
   public BinaryResourceModel transferFormStagingToPermanent(String value) {
      SongMetadataModel metadata = getDTO(value);
      Long resourceId = metadata.getResourceId();
      StorageObject stagingStorage = getStorageByType(StorageType.STAGING);

      BinaryResourceModel binaryResourceModel = uploadedContentRepository.getBinaryResourceModelByResourceId(resourceId);
      S3ObjectInputStream audioBinaryData;
      try {
         audioBinaryData = getDataFromBucket(binaryResourceModel, stagingStorage);
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return binaryResourceModel;
      }

      byte[] bytes = new byte[0];
      try {
         bytes = audioBinaryData.getDelegateStream().readAllBytes();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      InputStream stream = new ByteArrayInputStream(bytes);
      uploadDataToBucket(binaryResourceModel.getName(), stream, StorageType.PERMANENT);

      deleteDataFromBucket(binaryResourceModel.getName(), stagingStorage);

      binaryResourceModel.setStorageType(StorageType.PERMANENT.name());

      return binaryResourceModel;
   }

}
