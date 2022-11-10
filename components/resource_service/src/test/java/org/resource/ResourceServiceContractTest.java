package org.resource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ResourceServiceContractTest {

   @LocalServerPort
   private int port;

   @Autowired
   private ServerProperties serverProperties;

   private static final String MEDIA_TYPE = "audio/mpeg";

   @BeforeEach
   public void setUp() {
      port = serverProperties.getPort();
   }

   @Test
   void test() throws IOException {
      File file = new File(getClass().getClassLoader().getResource("testSong.mp3").getFile());
      FileBody fileBody = new FileBody(file, MEDIA_TYPE);
      HttpEntity entity = MultipartEntityBuilder.create()
            .addPart("data", fileBody)
            .build();

      HttpUriRequest postRequest = RequestBuilder.post()
            .setUri("http://localhost:" + port + "/resources")
            .setEntity(entity)
            .build();

      HttpResponse postResponse = HttpClientBuilder.create().build().execute(postRequest);
      byte[] postResponseData = postResponse.getEntity().getContent().readAllBytes();
      System.err.println(new String(postResponseData, StandardCharsets.UTF_8));
      String postResponseResult = new String(postResponseData, StandardCharsets.UTF_8);
      Long id = Long.parseLong(postResponseResult);

      HttpUriRequest getRequest = RequestBuilder.get()
            .setUri("http://localhost:" + port + "/resources/" + id)
            .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .build();
      HttpResponse getResponse = HttpClientBuilder.create().build().execute(getRequest);

      byte[] getResponseData = getResponse.getEntity().getContent().readAllBytes();
      System.err.println(new String(getResponseData, StandardCharsets.UTF_8));

      HttpUriRequest deleteRequest = RequestBuilder.delete()
            .setUri("http://localhost:" + port + "/resources?id=" + id)
            .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .build();
      HttpResponse deleteResponse = HttpClientBuilder.create().build().execute(deleteRequest);

      byte[] deleteResponseData = deleteResponse.getEntity().getContent().readAllBytes();
      String deleteResponseResult = new String(deleteResponseData, StandardCharsets.UTF_8);
      System.err.println(new String(deleteResponseData, StandardCharsets.UTF_8));

      assertTrue(deleteResponseResult.contains(postResponseResult));

   }
}
