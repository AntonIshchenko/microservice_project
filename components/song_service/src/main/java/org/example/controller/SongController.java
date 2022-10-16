package org.example.controller;

import org.example.model.SongMetadataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/songs", method = { RequestMethod.DELETE, RequestMethod.GET, RequestMethod.POST})
public class SongController {

   @Autowired
   public SongController() {}

   @PostMapping(path = "")
   public Integer createNewSong(SongMetadataModel model) {
      return 0;
   }

   @GetMapping(path = "/{id}")
   public SongMetadataModel getSongMetadata(@PathVariable Long id) {
      SongMetadataModel model = new SongMetadataModel();
      model.setResourceId(Integer.parseInt(id.toString()));
      return model;
   }

   @DeleteMapping(path = "")
   public List<Long> deleteSongsMetadata(@RequestParam List<Long> id) {
      return id;
   }
}
