package org.example.repository;

import org.example.model.SongMetadataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongModelRepository extends JpaRepository<SongMetadataModel, Long> {

   SongMetadataModel findSongMetadataModelByResourceId(Long id);

   void deleteByResourceId(Long resourceId);
}
