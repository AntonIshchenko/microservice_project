package org.resource;

import org.junit.jupiter.api.Test;
import org.resource.controller.ResourceController;
import org.resource.helper.TestMultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ResourceServiceApplicationTest {

   @Autowired
   private ResourceController resourceController;

   @Test
   void contextLoads() throws IOException {
      MultipartFile multipartFile = new TestMultipartFile();

      Long resourceId = resourceController.uploadNewResource(multipartFile);
      assertNotNull(resourceId);

      int audioBinaryData = resourceController.getAudioBinaryData(resourceId, Collections.emptyList());
      assertEquals(multipartFile.getBytes().length, audioBinaryData);

      List<Long> longs = resourceController.deleteSongs(Collections.singletonList(resourceId));
      assertEquals(resourceId, longs.get(0));

   }

}
