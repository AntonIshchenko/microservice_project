package org.example.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.example.model.SongMetadataModel;
import org.example.serivice.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
public class SongController {


   private final SongService songService;

   @Autowired
   public SongController(SongService songService) {
      this.songService = songService;
   }

   @ApiResponses(
         value = {
               @ApiResponse(code = 200, message = "OK"),
               @ApiResponse(code = 400, message = "Validation error missing metadata"),
               @ApiResponse(code = 500, message = "Internal server error occurred.")
         })
   @PostMapping(path = "/songs", consumes = "application/json", produces = "application/json")
   public Integer createNewSong(@RequestBody SongMetadataModel model) {
      return songService.createNewSongMetadata(model);
   }

   @ApiResponses(
         value = {
               @ApiResponse(code = 200, message = "OK"),
               @ApiResponse(code = 404, message = "Resource doesn’t exist with given id"),
               @ApiResponse(code = 500, message = "Internal server error occurred.")
         })
   @GetMapping(path = "/songs/{id}", produces = "application/json")
   public SongMetadataModel getSongMetadata(@PathVariable Long id) {
      return songService.getSongMetadata(id);
   }

   @ApiResponses(
         value = {
               @ApiResponse(code = 200, message = "OK"),
               @ApiResponse(code = 500, message = "Internal server error occurred.")
         })
   @DeleteMapping(path = "/songs", produces = "application/json")
   public List<Long> deleteSongsMetadata(@RequestParam List<Long> id) {
      return songService.deleteSongMetadata(id);
   }
}
