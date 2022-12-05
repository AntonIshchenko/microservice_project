package org.example.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
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
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.example.model.MetadataModeDTO;
import org.example.model.ResourceServiceMessage;
import org.example.model.SongMetadataModel;
import org.example.model.StorageObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Service
public class ResourceProcessorService {

   private static final String STORAGE_REGION = Regions.US_EAST_1.getName();

   @Value("${s3.storage.url}")
   private String storageUrl;
   @Value("${storage.service.url}")
   private String storageServiceUrl;

   private final KafkaTemplate<String, String> kafkaTemplate;
   private final ObjectMapper objectMapper;
   private AWSS3Service awsService;

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
      ListenableFuture<SendResult<String, String>> sendResultListenableFuture = sendMessage(new MetadataModeDTO(RequestMethod.POST, new SongMetadataModel()));
      try {
         result += sendResultListenableFuture.get().getProducerRecord().value();
      } catch (InterruptedException | ExecutionException e) {
         System.err.println(e.getMessage());
      }

      return result;
   }

   @SneakyThrows
   @Transactional
   public void sendMetadata(ResourceServiceMessage model) {
      StorageObject storageByType = getStorageByType(model.getStorageType());
      S3Object s3Object = awsService.getObject(storageByType.getBucket(), model.getName());
      S3ObjectInputStream objectContent = s3Object.getObjectContent();
      InputStream dataStream = objectContent.getDelegateStream();
      SongMetadataModel resultModel = retrieveMP3Metadata(model.getName(), dataStream);
      resultModel.setResourceId(model.getResourceId());
      sendMessage(new MetadataModeDTO(model.getMethod(), resultModel));
   }

   public void deleteMetadata(ResourceServiceMessage model) {
      SongMetadataModel metadataModel = new SongMetadataModel();
      metadataModel.setResourceId(model.getResourceId());
      metadataModel.setName(model.getName());
      sendMessage(new MetadataModeDTO(model.getMethod(), metadataModel));
   }

   public ResourceServiceMessage getDTO(String inputJson) {
      try {
         return objectMapper.readValue(inputJson, ResourceServiceMessage.class);
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return new ResourceServiceMessage();
      }
   }

   @SneakyThrows
   public ListenableFuture<SendResult<String, String>> sendMessage(MetadataModeDTO modelDTO) {
      String messageKey = modelDTO.getClass().getSimpleName() + "|" + modelDTO.getMethod();
      String messageValue = objectMapper.writeValueAsString(modelDTO);
      return kafkaTemplate.send("song-service.entityJson", messageKey, messageValue);
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

   @SneakyThrows
   private StorageObject getStorageByType(String storageType) {
      HttpUriRequest postRequest = RequestBuilder.get()
            .setUri(storageServiceUrl)
            .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .addParameter("storageType", storageType)
            .build();
      HttpResponse postResponse = HttpClientBuilder.create().build().execute(postRequest);

      String response = new String(postResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
      StorageObject storageObject = objectMapper.readValue(response, StorageObject.class);
      return storageObject;
   }

}
