package org.resource.service;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.resource.model.BinaryResourceModel;
import org.resource.repository.UploadedContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ResourceService {

   private final UploadedContentRepository uploadedContentRepository;

   @Autowired
   public ResourceService(UploadedContentRepository uploadedContentRepository) {
      this.uploadedContentRepository = uploadedContentRepository;
   }
   public Long uploadNewResource(MultipartFile data) {
      if(data.isEmpty()) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error or request body is an invalid MP3");
      }
      BinaryResourceModel model = uploadedContentRepository.save(new BinaryResourceModel());
      // write byte stream to cloud storage
      return model.getResourceId();
   }

   public MultipartFile getAudioBinaryData(Long id) {
      BinaryResourceModel model = uploadedContentRepository.getReferenceById(id);

//      get byte file by modelid
      return null;
   }

   @Transactional
   public List<Long> deleteSongs(List<Long> id) {
      for (Long e : id) {
         BinaryResourceModel model = uploadedContentRepository.getReferenceById(e);
         if(model == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error missing metadata");
         }
         uploadedContentRepository.deleteById(model.getResourceId());
      }
      return id;
   }
}
