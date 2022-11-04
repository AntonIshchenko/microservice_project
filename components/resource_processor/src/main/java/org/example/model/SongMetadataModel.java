package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SongMetadataModel {

   private long id;
   private String year;
   private String name;
   private String artist;
   private String album;
   private String length;
   private Long resourceId;

}
