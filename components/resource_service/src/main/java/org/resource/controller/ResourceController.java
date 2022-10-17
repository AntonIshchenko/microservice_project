package org.resource.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.awt.PageAttributes;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
public class ResourceController {

   private final ResourceService resourceService;

   @Autowired
   public ResourceController(ResourceService resourceService){
      this.resourceService = resourceService;
   }

   @ApiResponses(
         value = {
               @ApiResponse(code = 200, message = "OK"),
               @ApiResponse(code = 400, message = "Validation error or request body is an invalid MP3"),
               @ApiResponse(code = 500, message = "Internal server error occurred.")
         })
   @PostMapping(path = "/resources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public Long uploadNewResource(@RequestPart MultipartFile data) {
      return resourceService.uploadNewResource(data);
   }

   @ApiResponses(
         value = {
               @ApiResponse(code = 200, message = "OK"),
               @ApiResponse(code = 206, message = "Partial content (if range requested)"),
               @ApiResponse(code = 404, message = "Resource doesn’t exist with given id"),
               @ApiResponse(code = 500, message = "Internal server error occurred.")
         })
   @GetMapping(path = "/resources/{id}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
   public MultipartFile getAudioBinaryData(@PathVariable Long id) {
      return resourceService.getAudioBinaryData(id);
   }

   @ApiResponses(
         value = {
               @ApiResponse(code = 200, message = "OK"),
               @ApiResponse(code = 500, message = "Internal server error occurred.")
         })
   @DeleteMapping(path = "/resources", produces = "application/json")
   public List<Long> deleteSongs(@RequestParam List<Long> id) {
      return resourceService.deleteSongs(id);
   }

}
