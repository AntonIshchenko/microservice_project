package org.resource.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "storage_objects")
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StorageObject {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;
   private String storageType;
   private String bucket;
   private String path;

}
