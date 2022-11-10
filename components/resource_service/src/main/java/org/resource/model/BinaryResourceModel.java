package org.resource.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BINARY_RESOURCE_IDS", schema = "resources_data")
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BinaryResourceModel {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long resourceId;
   @Column(name = "SONG_YEAR")
   private String name;
   private RequestMethod method;

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;

      if (!(o instanceof BinaryResourceModel))
         return false;

      BinaryResourceModel that = (BinaryResourceModel) o;

      return new EqualsBuilder().append(resourceId, that.resourceId).append(name, that.name).isEquals();
   }

   @Override
   public int hashCode() {
      return new HashCodeBuilder(17, 37).append(resourceId).append(name).toHashCode();
   }
}
