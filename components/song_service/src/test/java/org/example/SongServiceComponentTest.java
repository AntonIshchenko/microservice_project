package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.example.model.SongMetadataModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SongServiceComponentTest {

   @LocalServerPort
   private int port;

   @Autowired
   private ServerProperties serverProperties;
   @Autowired
   private ObjectMapper objectMapper;
   @Autowired
   private KafkaTemplate<String, String> kafkaTemplate;

   private static final Long ID = Long.MAX_VALUE;

   @BeforeEach
   public void setUp() {
      port = serverProperties.getPort();
   }

   @Test
   void serviceTest_whenSendHTTPRequestsToCUDEndpoints_thenGetSuccessfulResult() throws IOException {
      String messageValue = getStringModel();

      HttpUriRequest postRequest = RequestBuilder.post()
            .setUri("http://localhost:" + port + "/songs")
            .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .setEntity(new StringEntity(messageValue))
            .build();
      HttpResponse postResponse = HttpClientBuilder.create().build().execute(postRequest);

      byte[] postResponseData = postResponse.getEntity().getContent().readAllBytes();
      Long result = Long.valueOf(new String(postResponseData, StandardCharsets.UTF_8));

      assertEquals(ID, result);

      HttpUriRequest getRequest = RequestBuilder.get()
            .setUri("http://localhost:" + port + "/songs/" + ID)
            .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .build();
      HttpResponse getResponse = HttpClientBuilder.create().build().execute(getRequest);

      byte[] getResponseData = getResponse.getEntity().getContent().readAllBytes();
      SongMetadataModel getResponseResult = objectMapper.readValue(getResponseData, SongMetadataModel.class);

      assertEquals(getMetadataModel(), getResponseResult);

      HttpUriRequest deleteRequest = RequestBuilder.delete()
            .setUri("http://localhost:" + port + "/songs?id=" + ID)
            .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .build();
      HttpResponse deleteResponse = HttpClientBuilder.create().build().execute(deleteRequest);

      byte[] deleteResponseData = deleteResponse.getEntity().getContent().readAllBytes();
      String deleteStringValue = new String(deleteResponseData, StandardCharsets.UTF_8);

      assertEquals(Arrays.toString(new Long[] { ID }), deleteStringValue);
   }

   private String getStringModel() throws JsonProcessingException {
      return objectMapper.writeValueAsString(getMetadataModel());
   }

   private SongMetadataModel getMetadataModel() {
      SongMetadataModel metadataModel = new SongMetadataModel();
      metadataModel.setName("songName");
      metadataModel.setAlbum("songAlbum");
      metadataModel.setLength("2:50");
      metadataModel.setArtist("songArtist");
      metadataModel.setYear("1999");
      metadataModel.setResourceId(ID);
      return metadataModel;
   }

}
