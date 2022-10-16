package org.example.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SongMetadataModel {

   private String name;
   private String artist;
   private String album;
   private String length;
   private Integer resourceId;
   private Integer year;

}
