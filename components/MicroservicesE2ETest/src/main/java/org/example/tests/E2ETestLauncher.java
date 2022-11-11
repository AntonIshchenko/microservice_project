package org.example.tests;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class E2ETestLauncher {

   private static final int PORT = 8888;

   private static final String MEDIA_TYPE = "audio/mpeg";

   public E2ETestLauncher() {
   }

   public void runTest() throws Exception {
      File file = new File(getClass().getClassLoader().getResource("testSong.mp3").getFile());
      FileBody fileBody = new FileBody(file, MEDIA_TYPE);
      HttpEntity entity = MultipartEntityBuilder.create()
            .addPart("data", fileBody)
            .build();

      HttpUriRequest postRequest = RequestBuilder.post()
            .setUri("http://localhost:" + PORT + "/resources")
            .setEntity(entity)
            .build();

      HttpResponse postResponse = HttpClientBuilder.create().build().execute(postRequest);
      byte[] postResponseData = postResponse.getEntity().getContent().readAllBytes();
      System.err.println(new String(postResponseData, StandardCharsets.UTF_8));
      String postResponseResult = new String(postResponseData, StandardCharsets.UTF_8);
      Long id = Long.parseLong(postResponseResult);

      HttpUriRequest getRequest = RequestBuilder.get()
            .setUri("http://localhost:" + PORT + "/resources/" + id)
            .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .build();
      HttpResponse getResponse = HttpClientBuilder.create().build().execute(getRequest);

      byte[] getResponseData = getResponse.getEntity().getContent().readAllBytes();
      System.err.println(new String(getResponseData, StandardCharsets.UTF_8));

      HttpUriRequest deleteRequest = RequestBuilder.delete()
            .setUri("http://localhost:" + PORT + "/resources?id=" + id)
            .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .build();
      HttpResponse deleteResponse = HttpClientBuilder.create().build().execute(deleteRequest);

      byte[] deleteResponseData = deleteResponse.getEntity().getContent().readAllBytes();
      String deleteResponseResult = new String(deleteResponseData, StandardCharsets.UTF_8);
      System.err.println(deleteResponseResult);

      if(!deleteResponseResult.contains(postResponseResult)) {
         throw new Exception("Test Failed");
      }

   }
}
