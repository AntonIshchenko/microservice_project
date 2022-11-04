package org.resource.repository;

import org.resource.model.BinaryResourceModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedContentRepository extends JpaRepository<BinaryResourceModel, Long> {

   BinaryResourceModel getBinaryResourceModelByName(String name);

   BinaryResourceModel getBinaryResourceModelByResourceId(Long name);

   void deleteBinaryResourceModelByResourceId(Long id);

}
