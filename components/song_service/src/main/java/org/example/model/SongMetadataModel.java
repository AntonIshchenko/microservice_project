package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "METADATA")
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SongMetadataModel {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;
   @Column(name = "SONG_YEAR")
   private Integer year;
   private String name;
   private String artist;
   private String album;
   private String length;
   @Column(unique = true)
   private Long resourceId;

}
