package org.example;

import org.example.controller.SongController;
import org.example.model.SongMetadataModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SongServiceApplicationTests {

   @Autowired
   public SongController songController;

   public static final String VALIDATION_EXCEPTION = "400 BAD_REQUEST \"Validation error missing metadata\"";

   @Test
   void contextLoads() {
      SongMetadataModel metadataModel = new SongMetadataModel();
      metadataModel.setName("songName");
      metadataModel.setAlbum("songAlbum");
      metadataModel.setLength("2:50");
      metadataModel.setArtist("songArtist");
      metadataModel.setYear("1999");
      metadataModel.setResourceId(Long.MAX_VALUE);

      Long newSong = songController.createNewSong(metadataModel);
      assertEquals(Long.MAX_VALUE, newSong);

      SongMetadataModel songMetadata = songController.getSongMetadata(Long.MAX_VALUE);
      assertEquals(metadataModel.getName(), songMetadata.getName());
      assertEquals(metadataModel.getArtist(), songMetadata.getArtist());
      assertEquals(metadataModel.getLength(), songMetadata.getLength());

      songController.deleteSongsMetadata(Collections.singletonList(Long.MAX_VALUE));
      try {
         songController.getSongMetadata(Long.MAX_VALUE);
         fail("Exception expected");
      } catch (ResponseStatusException e) {
         assertEquals(VALIDATION_EXCEPTION, e.getMessage());
      }
   }

}
