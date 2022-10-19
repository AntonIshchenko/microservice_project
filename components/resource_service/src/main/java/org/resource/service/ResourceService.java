package org.resource.service;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.resource.model.BinaryResourceModel;
import org.resource.repository.UploadedContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResourceService {

   private static final String STORAGE_URL = "http://localhost:4566";
   private static final String STORAGE_REGION = Regions.US_EAST_1.getName();
   private static final String BUCKET_NAME = "songs-bucket";

   private AWSS3Service awsService;

   @Autowired
   private UploadedContentRepository uploadedContentRepository;

   @PostConstruct
   private void initializeAWSS3Storage() {
      AmazonS3 amazonS3 = AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(STORAGE_URL, STORAGE_REGION))
            .withPathStyleAccessEnabled(true)
            .build();
      awsService = new AWSS3Service(amazonS3);

      //creating a bucket
      if(!awsService.doesBucketExist(BUCKET_NAME)) {
         amazonS3.createBucket(BUCKET_NAME);
      }
   }

   public Long uploadNewResource(MultipartFile data) throws IOException {
      if(data.isEmpty()) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error or request body is an invalid MP3");
      }
      BinaryResourceModel resourceModel = new BinaryResourceModel();
      resourceModel.setFileName(data.getResource().getFilename());
      resourceModel.setFileSize(data.getResource().contentLength());

      BinaryResourceModel model = uploadedContentRepository.save(resourceModel);

      if(!uploadDataToBucket(resourceModel, data)) {
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error occurred.");
      }
      return model.getResourceId();
   }

   public S3ObjectInputStream getAudioBinaryData(Long id) {
      BinaryResourceModel model = uploadedContentRepository.getReferenceById(id);
      try {
         return getDataFromBucket(model);
      } catch (EntityNotFoundException e) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource doesn’t exist with given id");
      }
   }

   @Transactional
   public List<Long> deleteSongs(List<Long> id) {
      for (Long e : id) {
         BinaryResourceModel model = uploadedContentRepository.getReferenceById(e);
         uploadedContentRepository.deleteById(model.getResourceId());
         deleteDataFromBucket(model);
      }
      return id;
   }

   private boolean uploadDataToBucket(BinaryResourceModel resourceModel, MultipartFile data) throws IOException {
      List<String> keys = new ArrayList<>();

      awsService.listObjects(BUCKET_NAME).getObjectSummaries().forEach(e -> keys.add(e.getKey()));
      if(keys.contains(resourceModel.getFileName())) {
         return true;
      }
      return awsService.putObject(BUCKET_NAME, resourceModel.getFileName(), data.getInputStream()).getVersionId() != null;
   }

   private S3ObjectInputStream getDataFromBucket(BinaryResourceModel resourceModel) {
      S3Object s3Object = awsService.getObject(BUCKET_NAME, resourceModel.getFileName());
      return s3Object.getObjectContent();
   }

   private void deleteDataFromBucket(BinaryResourceModel resourceModel) {
      awsService.deleteObject(BUCKET_NAME, resourceModel.getFileName());
   }

}
