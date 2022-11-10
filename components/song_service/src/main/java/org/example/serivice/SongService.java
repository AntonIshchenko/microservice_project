package org.example.serivice;

import lombok.RequiredArgsConstructor;
import org.example.model.SongMetadataModel;
import org.example.repository.SongModelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongService {

   private final SongModelRepository songModelRepository;

   public Long createNewSongMetadata(SongMetadataModel model) {
      if (model.getResourceId() == null) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error missing metadata");
      }
      songModelRepository.save(model);
      return model.getResourceId();
   }

   public SongMetadataModel getSongMetadata(Long id) {
      SongMetadataModel metadataModel = songModelRepository.findSongMetadataModelByResourceId(id);
      if (metadataModel == null) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error missing metadata");
      }
      return metadataModel;
   }

   @Transactional
   public List<Long> deleteSongMetadata(List<Long> id) {
      for (Long e : id) {
         SongMetadataModel model = songModelRepository.findSongMetadataModelByResourceId(e);
         if (model == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error missing metadata");
         }
         songModelRepository.deleteById(model.getId());
      }
      return id;
   }

}
