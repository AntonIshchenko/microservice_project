package org.resource.service;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.resource.model.BinaryResourceModel;
import org.resource.repository.UploadedContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
      if (!awsService.doesBucketExist(BUCKET_NAME)) {
         awsService.createBucket(BUCKET_NAME);
      }
   }

   public Long uploadNewResource(MultipartFile data) {
      InputStream dataStream;
      try {
         dataStream = data.getInputStream();
      } catch (IOException e) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error or request body is an invalid MP3", e.getCause());
      }

      BinaryResourceModel resourceModel = retrieveMP3Metadata(data, dataStream);
      BinaryResourceModel ifExist = uploadedContentRepository.getBinaryResourceModelByName(resourceModel.getName());
      if(ifExist != null) {
         return ifExist.getResourceId();
      }

      BinaryResourceModel model = uploadedContentRepository.save(resourceModel);

      if (!uploadDataToBucket(resourceModel, dataStream)) {
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

   private boolean uploadDataToBucket(BinaryResourceModel resourceModel, InputStream data) {
      List<String> keys = new ArrayList<>();

      awsService.listObjects(BUCKET_NAME).getObjectSummaries().forEach(e -> keys.add(e.getKey()));
      if (keys.contains(resourceModel.getName())) {
         return true;
      }
      PutObjectResult putObjectResult = awsService.putObject(BUCKET_NAME, resourceModel.getName(), data);
      return putObjectResult.getETag() != null;
   }

   private S3ObjectInputStream getDataFromBucket(BinaryResourceModel resourceModel) {
      S3Object s3Object = awsService.getObject(BUCKET_NAME, resourceModel.getName());
      return s3Object.getObjectContent();
   }

   private void deleteDataFromBucket(BinaryResourceModel resourceModel) {
      awsService.deleteObject(BUCKET_NAME, resourceModel.getName());
   }

   private BinaryResourceModel retrieveMP3Metadata(MultipartFile fileData, InputStream data) {
      BinaryResourceModel resultModel = new BinaryResourceModel();

      ContentHandler handler = new DefaultHandler();
      Metadata metadata = new Metadata();
      Parser parser = new Mp3Parser();
      ParseContext parseCtx = new ParseContext();
      try {
         parser.parse(data, handler, metadata, parseCtx);
      } catch (IOException | SAXException | TikaException e) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error or request body is an invalid MP3", e.getCause());
      }

      resultModel.setName(fileData.getOriginalFilename());
      resultModel.setArtist(metadata.get("xmpDM:artist"));
      resultModel.setAlbum(metadata.get("xmpDM:album"));
      resultModel.setYear(metadata.get("xmpDM:releaseDate"));

      double duration = 0;
      try {
         duration = Double.parseDouble(metadata.get("xmpDM:duration"));
      } catch (NumberFormatException | NullPointerException e) {
         resultModel.setLength("");
      }
      resultModel.setLength(convertTrackDuration((int) duration));
      return resultModel;
   }

   private String convertTrackDuration(Integer duration) {
      int minutes = duration / (60);
      int seconds = duration % 60;
      return String.format("%d:%02d", minutes, seconds);
   }

   private void clearBucket() {
      List<String> keys = new ArrayList<>();
      awsService.listObjects(BUCKET_NAME).getObjectSummaries().forEach(e -> keys.add(e.getKey()));
      keys.forEach(e -> awsService.deleteObject(BUCKET_NAME, e));
   }
}
