package org.resource.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "STORAGE_OBJECTS")
@Builder
@Data
public class StorageObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private final Long id;
    private final String storageType;
    private final String bucket;
    private final String path;

}
