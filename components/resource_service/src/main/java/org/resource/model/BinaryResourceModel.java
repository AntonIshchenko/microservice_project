package org.resource.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BINARY_RESOURCE_IDS")
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

}
