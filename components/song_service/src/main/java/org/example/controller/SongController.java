package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.model.MetadataModeDTO;
import org.example.model.SongMetadataModel;
import org.example.serivice.SongService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SongController {

   private final ObjectMapper objectMapper;
   private final SongService songService;

   @PostMapping(path = "/songs", consumes = "application/json", produces = "application/json")
   public Integer createNewSong(@RequestBody SongMetadataModel model) {
      return songService.createNewSongMetadata(model);
   }

   @GetMapping(path = "/songs/{id}", produces = "application/json")
   public SongMetadataModel getSongMetadata(@PathVariable Long id) {
      return songService.getSongMetadata(id);
   }

   @DeleteMapping(path = "/songs", produces = "application/json")
   public List<Long> deleteSongsMetadata(@RequestParam List<Long> id) {
      return songService.deleteSongMetadata(id);
   }

   @SneakyThrows
   @KafkaListener(id = "entityJSONListener",
         containerFactory = "jsonEntityConsumerFactory",
         topics = "song-service.entityJson",
         groupId = "search.entity-json-consumer")
   public void handleMessage(String value) {
      MetadataModeDTO messageObject = objectMapper.readValue(value, MetadataModeDTO.class);
      if (messageObject.getMethod() == RequestMethod.POST) {
         createNewSong(messageObject.getModel());
      } else if (messageObject.getMethod() == RequestMethod.DELETE) {
         deleteSongsMetadata(List.of(messageObject.getModel().getResourceId()));
      }
      System.err.println(value);
   }

}
