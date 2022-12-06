package org.resource.controller;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.RequiredArgsConstructor;
import org.resource.model.BinaryResourceModel;
import org.resource.model.StorageType;
import org.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.io.IOUtils.toByteArray;

@RestController
@RequiredArgsConstructor
public class ResourceController {

   private static final String MEDIA_TYPE = "audio/mpeg";

   @Autowired
   private ResourceService resourceService;

   @GetMapping(path = "/echo", produces = "application/json")
   public String echo() {
      return resourceService.echo();
   }

   @PostMapping(path = "/resources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public Long uploadNewResource(@RequestPart MultipartFile data) {
      if (data.getContentType() == null || MEDIA_TYPE.compareToIgnoreCase(data.getContentType()) != 0)
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error or request body is an invalid MP3");
      return resourceService.uploadNewResource(data);
   }

   @GetMapping(path = "/resources/{id}", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public int getAudioBinaryData(@PathVariable Long id, @RequestParam(required = false, defaultValue = "") List<Integer> range) {
      if (range != null && !range.isEmpty())
         return Objects.requireNonNull(resourceService.getAudioBinaryDataWithRange(id, range, StorageType.PERMANENT).getBody()).length;

      try {
         S3ObjectInputStream audioBinaryData = resourceService.getAudioBinaryData(id, StorageType.PERMANENT);
         byte[] bytes = toByteArray(audioBinaryData.getDelegateStream());
         return bytes.length;
      } catch (IOException e) {
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error occurred.");
      }
   }

   @GetMapping(path = "/resources/model/{id}")
   public BinaryResourceModel getModelById(@PathVariable Long id) {
      return resourceService.getResourceModelByID(id);
   }

   @DeleteMapping(path = "/resources", produces = "application/json")
   public List<Long> deleteSongs(@RequestParam List<Long> id) {
      return resourceService.deleteSongs(id, StorageType.PERMANENT);
   }

   @KafkaListener(id = "entityJSONListener",
         containerFactory = "jsonEntityConsumerFactory",
         topics = "storage-transfer-to-permanent.entityJson",
         groupId = "search.entity-json-consumer")
   public void handleMessage(String value) {
      BinaryResourceModel binaryResourceModel = resourceService.transferFormStagingToPermanent(value);
      System.err.println(binaryResourceModel);
   }
}
