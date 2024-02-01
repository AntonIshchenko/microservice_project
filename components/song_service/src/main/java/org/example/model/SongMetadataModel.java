package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


@Entity
@Table(name = "songsmetadata")
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SongMetadataModel {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;
   @Column(name = "song_year")
   private String year;
   private String name;
   private String artist;
   private String album;
   private String length;
   @Column(unique = true)
   private Long resourceId;

   @Override public boolean equals(Object o) {
      if (this == o)
         return true;

      if (!(o instanceof SongMetadataModel))
         return false;

      SongMetadataModel that = (SongMetadataModel) o;

      return new EqualsBuilder().append(year, that.year).append(name, that.name).append(artist, that.artist).append(album, that.album)
            .append(length, that.length).append(resourceId, that.resourceId).isEquals();
   }

   @Override public int hashCode() {
      return new HashCodeBuilder(17, 37).append(year).append(name).append(artist).append(album).append(length).append(resourceId).toHashCode();
   }

}
