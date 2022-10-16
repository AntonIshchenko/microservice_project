package org.example.serivice;

import org.example.model.SongMetadataModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongService {

   public Integer createNewSongMetadata(SongMetadataModel model) {
      return 1;
   }

   public SongMetadataModel getSongMetadata(Long id) {
      SongMetadataModel model = new SongMetadataModel();
      model.setResourceId(id.intValue());
      return model;
   }

   public List<Long> deleteSongMetadata(List<Long> id) {
      return id;
   }

}
