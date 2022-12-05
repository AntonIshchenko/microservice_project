package com.example.storageservice.repository;

import com.example.storageservice.model.StorageObject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageServiceRepository extends JpaRepository<StorageObject, Long> {

   StorageObject getStorageObjectByStorageType(String storageType);

   void deleteStorageObjectById(Long id);

}
