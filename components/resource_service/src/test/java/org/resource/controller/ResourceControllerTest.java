package org.resource.controller;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.resource.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceControllerTest {

   @Mock
   private ResourceService resourceService;
   @InjectMocks
   private ResourceController resourceController;

   private static final String MEDIA_TYPE = "audio/mpeg";
   public static final String VALIDATION_EXCEPTION = "400 BAD_REQUEST \"Validation error or request body is an invalid MP3\"";
   public static final List<Long> IDS = Collections.singletonList(1L);
   public static final Long ID = 1L;
   public static final List<Integer> RANGE = Arrays.asList(1, 2);
   public static final byte[] BYTES = { 1, 2, 3 };
   public static final S3ObjectInputStream S_3_OBJECT_INPUT_STREAM = mock(S3ObjectInputStream.class);

   @Test
   void uploadNewResource_whenUploadValidResource_thenUploadNewResourceInvoked() {
      MultipartFile file = mock(MultipartFile.class);
      when(file.getContentType()).thenReturn(MEDIA_TYPE);
      when(resourceService.uploadNewResource(file)).thenReturn(1L);

      resourceController.uploadNewResource(file);

      verify(resourceService).uploadNewResource(file);
   }

   @Test
   void uploadNewResource_whenUploadResourceWithNullMediaType_thenExceptionThrown() {
      MultipartFile file = mock(MultipartFile.class);
      when(file.getContentType()).thenReturn(null);
      try {
         resourceController.uploadNewResource(file);
         fail("Exception expected");
      } catch (ResponseStatusException e) {
         assertEquals(VALIDATION_EXCEPTION, e.getMessage());
      }
   }

   @Test
   void uploadNewResource_whenUploadResourceWithWrongMediaType_thenExceptionThrown() {
      MultipartFile file = mock(MultipartFile.class);
      when(file.getContentType()).thenReturn(MediaType.APPLICATION_JSON_VALUE);
      try {
         resourceController.uploadNewResource(file);
         fail("Exception expected");
      } catch (ResponseStatusException e) {
         assertEquals(VALIDATION_EXCEPTION, e.getMessage());
      }
   }

   @Test
   void getAudioBinaryData_whenRangeProvided_thenDataLengthReturned() {
      when(resourceService.getAudioBinaryDataWithRange(ID, RANGE)).thenReturn(new ResponseEntity<>(BYTES, HttpStatus.PARTIAL_CONTENT));
      int audioBinaryDataLength = resourceController.getAudioBinaryData(ID, RANGE);
      verify(resourceService).getAudioBinaryDataWithRange(ID, RANGE);
      assertEquals(BYTES.length, audioBinaryDataLength);
   }

   @Test
   void getAudioBinaryData_whenRangeNotProvidedAndValidData_thenDataLengthReturned() throws IOException {
      when(S_3_OBJECT_INPUT_STREAM.getDelegateStream()).thenReturn(new ByteArrayInputStream(BYTES));
      when(resourceService.getAudioBinaryData(ID)).thenReturn(S_3_OBJECT_INPUT_STREAM);

      int audioBinaryDataLength = resourceController.getAudioBinaryData(ID, emptyList());

      verify(resourceService).getAudioBinaryData(ID);
      assertEquals(BYTES.length, audioBinaryDataLength);
   }

   @Test
   void deleteSongs_whenGetListOfIds_thenServiceDeleteMethodInvoked() {
      when(resourceService.deleteSongs(IDS)).thenReturn(IDS);

      resourceController.deleteSongs(IDS);

      verify(resourceService).deleteSongs(IDS);
   }
}