package org.example.serivice;

import org.example.model.SongMetadataModel;
import org.example.repository.SongModelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SongServiceTest {

   @Mock
   private SongModelRepository songModelRepository;
   @InjectMocks
   private SongService songService;

   public static final SongMetadataModel METADATA_MODEL_WITH_ID = new SongMetadataModel();
   public static final SongMetadataModel METADATA_MODEL_WITHOUT_ID = new SongMetadataModel();
   public static final Long ID = 0L;
   public static final String VALIDATION_EXCEPTION = "400 BAD_REQUEST \"Validation error missing metadata\"";
   public static final List<Long> IDS_TO_DELETE = List.of(0L, 1L, 2L);

   @BeforeEach
   void init() {
      METADATA_MODEL_WITH_ID.setResourceId(ID);
   }

   @Test
   void createNewSongMetadata_whenResourceIdIsNull_thenThrowException() {
      try {
         songService.createNewSongMetadata(METADATA_MODEL_WITHOUT_ID);
         fail("Exception expected");
      } catch (ResponseStatusException e) {
         assertEquals(VALIDATION_EXCEPTION, e.getMessage());
      }
   }

   @Test
   void createNewSongMetadata_whenResourceIdIsNotNull_thenSaveMethodInvoked() {
      Long resourceID = songService.createNewSongMetadata(METADATA_MODEL_WITH_ID);

      verify(songModelRepository).save(METADATA_MODEL_WITH_ID);
      assertEquals(ID, resourceID);
   }

   @Test
   void getSongMetadata_whenResourceIsNull_thenThrowException() {
      when(songModelRepository.findSongMetadataModelByResourceId(ID)).thenReturn(null);
      try {
         songService.getSongMetadata(ID);
         fail("Exception expected");
      } catch (ResponseStatusException e) {
         assertEquals(VALIDATION_EXCEPTION, e.getMessage());
      }
   }

   @Test
   void getSongMetadata_whenResourceIsNotNull_thenReturnMetadataModel() {
      when(songModelRepository.findSongMetadataModelByResourceId(ID)).thenReturn(METADATA_MODEL_WITH_ID);
      SongMetadataModel songMetadata = songService.getSongMetadata(ID);
      assertEquals(ID, songMetadata.getResourceId());
      verify(songModelRepository).findSongMetadataModelByResourceId(ID);
   }

   @Test
   void deleteSongMetadata_whenOneOfResourcesIsNull_thenThrowException() {
      SongMetadataModel model0 = new SongMetadataModel();
      model0.setResourceId(0l);
      SongMetadataModel model2 = new SongMetadataModel();
      model2.setResourceId(2l);
      when(songModelRepository.findSongMetadataModelByResourceId(ID)).thenReturn(model0);
      when(songModelRepository.findSongMetadataModelByResourceId(ID)).thenReturn(null);
      when(songModelRepository.findSongMetadataModelByResourceId(ID)).thenReturn(model2);

      try {
         songService.deleteSongMetadata(IDS_TO_DELETE);
         fail("Exception expected");
      } catch (ResponseStatusException e) {
         verify(songModelRepository, times(2)).findSongMetadataModelByResourceId(anyLong());
         verify(songModelRepository).deleteById(anyLong());
         assertEquals(VALIDATION_EXCEPTION, e.getMessage());
      }
   }

   @Test
   void deleteSongMetadata_whenAllResourcesAreNotNull_thenReturnMetadataModel() {
      when(songModelRepository.findSongMetadataModelByResourceId(anyLong())).thenReturn(METADATA_MODEL_WITH_ID);

      songService.deleteSongMetadata(IDS_TO_DELETE);

      verify(songModelRepository, times(3)).findSongMetadataModelByResourceId(anyLong());
      verify(songModelRepository, times(3)).deleteById(anyLong());
   }

}