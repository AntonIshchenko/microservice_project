package org.example.serivice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.model.SongMetadataModel;
import org.example.repository.SongModelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class SongService {

   private final KafkaTemplate<String, String> kafkaTemplate;
   private final ObjectMapper objectMapper;
   private final SongModelRepository songModelRepository;

   public Long createNewSongMetadata(SongMetadataModel model) {
      if (model.getResourceId() == null) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error missing metadata");
      }
      songModelRepository.save(model);
      sendMessage(model);
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

   @SneakyThrows
   private CompletableFuture<SendResult<String, String>> sendMessage(SongMetadataModel model) {
      String messageKey = model.getClass().getSimpleName() + "|" + model.getName();
      String messageValue = objectMapper.writeValueAsString(model);
      return kafkaTemplate.send("storage-transfer-to-permanent.entityJson", messageKey, messageValue);
   }

}
