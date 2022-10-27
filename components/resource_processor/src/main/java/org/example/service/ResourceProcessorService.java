package org.example.service;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.example.model.SongMetadataModel;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@Service
public class ResourceProcessorService {

   private static final String STORAGE_URL = "http://localhost:4566";
   private static final String STORAGE_REGION = Regions.US_EAST_1.getName();
   private static final String BUCKET_NAME = "songs-bucket";

   private final KafkaTemplate<String, String> kafkaTemplate;
   private final ObjectMapper objectMapper;
   private AWSS3Service awsService;

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

   @SneakyThrows
   @Transactional
   public void handleSongMetadata(String inputMessage) {
      SongMetadataModel model = getFileModel(inputMessage);

      S3Object s3Object = awsService.getObject(BUCKET_NAME, model.getName());
      S3ObjectInputStream objectContent = s3Object.getObjectContent();
      InputStream dataStream = new ByteArrayInputStream(objectContent.readAllBytes());
      SongMetadataModel resultModel = retrieveMP3Metadata(model.getName(), dataStream);
      resultModel.setResourceId(model.getResourceId());
      sendMessage(model);
   }

   private SongMetadataModel getFileModel(String inputJson) {
      try {
         return objectMapper.readValue(inputJson, SongMetadataModel.class);
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return new SongMetadataModel();
      }
   }

   @SneakyThrows public void sendMessage(SongMetadataModel model) {
      var messageKey = model.getClass().getSimpleName() + "|" + model.getId();
      var messageValue = objectMapper.writeValueAsString(model);
      kafkaTemplate.send("song-service.entityJson", messageKey, messageValue);
   }

   private SongMetadataModel retrieveMP3Metadata(String fileName, InputStream data) {
      SongMetadataModel resultModel = new SongMetadataModel();

      ContentHandler handler = new DefaultHandler();
      Metadata metadata = new Metadata();
      Parser parser = new Mp3Parser();
      ParseContext parseCtx = new ParseContext();
      try {
         parser.parse(data, handler, metadata, parseCtx);
      } catch (IOException | SAXException | TikaException e) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error or request body is an invalid MP3", e.getCause());
      }

      resultModel.setName(fileName);
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

}
