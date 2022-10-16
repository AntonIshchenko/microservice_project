package org.example.serivice;

import org.example.model.SongMetadataModel;
import org.example.repository.SongModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongService {

   private final SongModelRepository songModelRepository;

   @Autowired
   public SongService(SongModelRepository songModelRepository) {
      this.songModelRepository = songModelRepository;
   }

   public Integer createNewSongMetadata(SongMetadataModel model) {
      songModelRepository.save(model);
      return Integer.parseInt(model.getResourceId().toString());
   }

   public SongMetadataModel getSongMetadata(Long id) {
      SongMetadataModel metadataModel = songModelRepository.findSongMetadataModelByResourceId(id);
      return metadataModel;
   }

   public List<Long> deleteSongMetadata(List<Long> id) {
      id.forEach(e -> songModelRepository.deleteById(songModelRepository.findSongMetadataModelByResourceId(e).getId()));
      return id;
   }

}
