package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.MetadataModeDTO;
import org.example.model.SongMetadataModel;
import org.example.serivice.SongService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SongControllerTest {

   @Mock
   private SongService songService;
   @Mock
   private ObjectMapper objectMapper;
   @InjectMocks
   private SongController songController;

   private final static SongMetadataModel SONG_METADATA_MODEL = new SongMetadataModel();
   public static final Long ID = 0L;
   public static final List<Long> IDS_LIST = Collections.singletonList(ID);

   @Test
   void createNewSong_whenCall_thenSongServiceInvoked() {
      when(songService.createNewSongMetadata(SONG_METADATA_MODEL)).thenReturn(0L);

      songController.createNewSong(SONG_METADATA_MODEL);

      verify(songService).createNewSongMetadata(SONG_METADATA_MODEL);
   }

   @Test
   void getSongMetadata_whenCall_thenSongServiceInvoked() {
      when(songService.getSongMetadata(ID)).thenReturn(SONG_METADATA_MODEL);

      songController.getSongMetadata(ID);

      verify(songService).getSongMetadata(ID);
   }

   @Test
   void deleteSongsMetadata_whenCall_thenSongServiceInvoked() {
      when(songService.deleteSongMetadata(IDS_LIST)).thenReturn(IDS_LIST);

      songController.deleteSongsMetadata(IDS_LIST);

      verify(songService).deleteSongMetadata(IDS_LIST);
   }

   @Test
   void handleMessage_whenCallWithPOSTMethod_thenSongServiceCreateMethodInvoked() throws JsonProcessingException {
      when(objectMapper.readValue("value", MetadataModeDTO.class)).thenReturn(new MetadataModeDTO(RequestMethod.POST, SONG_METADATA_MODEL));

      songController.handleMessage("value");

      verify(songService).createNewSongMetadata(SONG_METADATA_MODEL);
   }

   @Test
   void handleMessage_whenCallWithDELETEMethod_thenSongServiceDeleteMethodInvoked() throws JsonProcessingException {
      SONG_METADATA_MODEL.setResourceId(ID);
      when(objectMapper.readValue("value", MetadataModeDTO.class)).thenReturn(new MetadataModeDTO(RequestMethod.DELETE, SONG_METADATA_MODEL));

      songController.handleMessage("value");

      verify(songService).deleteSongMetadata(IDS_LIST);
   }

}