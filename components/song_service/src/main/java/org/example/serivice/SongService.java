package org.example.serivice;

import lombok.SneakyThrows;
import org.example.model.SongMetadataModel;
import org.example.repository.SongModelRepository;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class SongService {

   private final SongModelRepository songModelRepository;

   @Autowired
   public SongService(SongModelRepository songModelRepository) {
      this.songModelRepository = songModelRepository;
   }

   public Integer createNewSongMetadata(SongMetadataModel model) {
      if (model.getResourceId() == null) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error missing metadata");
      }
      songModelRepository.save(model);
      return Integer.parseInt(model.getResourceId().toString());
   }

   public SongMetadataModel getSongMetadata(Long id) {
      SongMetadataModel metadataModel = songModelRepository.findSongMetadataModelByResourceId(id);
      if(metadataModel == null) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error missing metadata");
      }
      return metadataModel;
   }

   @Transactional
   public List<Long> deleteSongMetadata(List<Long> id) {
      for (Long e : id) {
         SongMetadataModel model = songModelRepository.findSongMetadataModelByResourceId(e);
         if(model == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error missing metadata");
         }
         songModelRepository.deleteById(model.getId());
      }
      return id;
   }

}
