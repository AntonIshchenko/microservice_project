package org.resource.service;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.resource.model.BinaryResourceModel;
import org.resource.repository.UploadedContentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

   @Mock
   private UploadedContentRepository uploadedContentRepository;
   @Mock
   private ObjectMapper objectMapper;
   @Mock
   private AWSS3Service awsService;
   @Mock
   private KafkaTemplate<String, String> kafkaTemplate;
   @InjectMocks
   private ResourceService resourceService;

   public static final String FILE_NAME = "file_name";
   public static final String BAD_REQUEST = "400 BAD_REQUEST \"Invalid range\"";
   public static final String INTERNAL_SERVER_ERROR = "500 INTERNAL_SERVER_ERROR \"Internal server error occurred.\"";
   public static final String RESOURCE_NOT_FOUND = "404 NOT_FOUND \"Resource doesn’t exist with given id\"";
   public static final List<Integer> VALID_RANGE = List.of(1, 2);
   public static final List<Integer> INVALID_RANGE = List.of(2, 1);
   public static final List<Integer> INVALID_SIZE_RANGE = List.of(2, 1, 3);
   public static final Long ID = 1L;
   public static final List<Long> IDS = List.of(1L, 2L);
   public static final BinaryResourceModel RESOURCE_MODEL = new BinaryResourceModel(ID, FILE_NAME, RequestMethod.POST);

   @BeforeEach
   public void initMocks() {
      MockitoAnnotations.openMocks(this);
   }

   @Test
   void uploadNewResource_whenResourceAlreadyExist_thenReturnItsId() throws JsonProcessingException {
      MultipartFile file = mock(MultipartFile.class);
      when(file.getOriginalFilename()).thenReturn(FILE_NAME);
      when(uploadedContentRepository.getBinaryResourceModelByName(FILE_NAME)).thenReturn(RESOURCE_MODEL);
      lenient().when(objectMapper.writeValueAsString(RESOURCE_MODEL)).thenReturn("message");
      lenient().when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(null);

      Long resultID = resourceService.uploadNewResource(file);

      verify(uploadedContentRepository).getBinaryResourceModelByName(FILE_NAME);
      assertEquals(ID, resultID);
   }

   @Test
   void uploadNewResource_whenResourceNotExistButAlreadyUploaded_thenUploadAndReturnItsId() throws JsonProcessingException {
      MultipartFile file = mock(MultipartFile.class);
      when(file.getOriginalFilename()).thenReturn(FILE_NAME);
      when(uploadedContentRepository.getBinaryResourceModelByName(FILE_NAME)).thenReturn(RESOURCE_MODEL);
      lenient().when(uploadedContentRepository.save(RESOURCE_MODEL)).thenReturn(RESOURCE_MODEL);

      ObjectListing objectListing = mock(ObjectListing.class);
      lenient().when(awsService.listObjects(null)).thenReturn(objectListing);
      S3ObjectSummary s3ObjectSummary = mock(S3ObjectSummary.class);
      lenient().when(objectListing.getObjectSummaries()).thenReturn(List.of(s3ObjectSummary));
      lenient().when(s3ObjectSummary.getKey()).thenReturn(FILE_NAME);

      lenient().when(objectMapper.writeValueAsString(RESOURCE_MODEL)).thenReturn("message");
      lenient().when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(null);

      Long resultID = resourceService.uploadNewResource(file);

      verify(uploadedContentRepository).getBinaryResourceModelByName(FILE_NAME);
      assertEquals(ID, resultID);
   }

   @Test
   void uploadNewResource_whenResourceNotExistAndNotUploaded_thenUploadAndReturnItsId() throws JsonProcessingException {
      MultipartFile file = mock(MultipartFile.class);
      when(file.getOriginalFilename()).thenReturn(FILE_NAME);
      lenient().when(uploadedContentRepository.getBinaryResourceModelByName(FILE_NAME)).thenReturn(null);
      lenient().when(uploadedContentRepository.save(any(BinaryResourceModel.class))).thenReturn(RESOURCE_MODEL);

      ObjectListing objectListing = mock(ObjectListing.class);
      lenient().when(awsService.listObjects(null)).thenReturn(objectListing);
      lenient().when(objectListing.getObjectSummaries()).thenReturn(emptyList());
      PutObjectResult putObjectResult = new PutObjectResult();
      putObjectResult.setETag("tag");
      lenient().when(awsService.putObject(any(), any(), any())).thenReturn(putObjectResult);

      lenient().when(objectMapper.writeValueAsString(RESOURCE_MODEL)).thenReturn("message");
      lenient().when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(null);

      Long resultID = resourceService.uploadNewResource(file);

      verify(uploadedContentRepository).getBinaryResourceModelByName(FILE_NAME);
      assertEquals(ID, resultID);
   }

   @Test
   void uploadNewResource_whenResourceNotExistFailToUpload_thenTrowException() throws JsonProcessingException {
      MultipartFile file = mock(MultipartFile.class);
      when(file.getOriginalFilename()).thenReturn(FILE_NAME);
      when(uploadedContentRepository.getBinaryResourceModelByName(FILE_NAME)).thenReturn(null);
      lenient().when(uploadedContentRepository.save(RESOURCE_MODEL)).thenReturn(RESOURCE_MODEL);

      ObjectListing objectListing = mock(ObjectListing.class);
      lenient().when(awsService.listObjects(null)).thenReturn(objectListing);
      lenient().when(objectListing.getObjectSummaries()).thenReturn(emptyList());
      PutObjectResult putObjectResult = new PutObjectResult();
      putObjectResult.setETag(null);
      lenient().when(awsService.putObject(any(), any(), any())).thenReturn(putObjectResult);

      try {
         Long resultID = resourceService.uploadNewResource(file);
         fail("Exception expected");
      } catch (ResponseStatusException e) {
         assertEquals(INTERNAL_SERVER_ERROR, e.getMessage());
      }
   }

   @Test
   void getAudioBinaryData_whenNoDataWasRead_thenThrowException() {
      when(uploadedContentRepository.getBinaryResourceModelByResourceId(ID)).thenReturn(null);

      try {
         resourceService.getAudioBinaryData(ID);
         fail("Exception expected");
      } catch (ResponseStatusException e) {
         assertEquals(RESOURCE_NOT_FOUND, e.getMessage());
      }
   }

   @Test
   void getAudioBinaryData_whenNoDataWasRead_thenReturnInputStream() throws JsonProcessingException {
      S3Object s3Object = mock(S3Object.class);
      S3ObjectInputStream s3ObjectInputStream = mock(S3ObjectInputStream.class);
      when(uploadedContentRepository.getBinaryResourceModelByResourceId(ID)).thenReturn(RESOURCE_MODEL);
      when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);
      when(awsService.getObject(null, "file_name")).thenReturn(s3Object);

      lenient().when(objectMapper.writeValueAsString(RESOURCE_MODEL)).thenReturn("message");
      lenient().when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(null);

      S3ObjectInputStream audioBinaryData = resourceService.getAudioBinaryData(ID);

      assertEquals(s3ObjectInputStream, audioBinaryData);
      verify(uploadedContentRepository).getBinaryResourceModelByResourceId(ID);
      verify(awsService).getObject(null, "file_name");

   }

   @Test
   void deleteSongs_whenDeleteListOfSongs_thenDeleteMethodsInvoked() throws JsonProcessingException {
      when(uploadedContentRepository.getBinaryResourceModelByResourceId(anyLong())).thenReturn(RESOURCE_MODEL);
      lenient().when(objectMapper.writeValueAsString(RESOURCE_MODEL)).thenReturn("message");
      lenient().when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(null);

      resourceService.deleteSongs(IDS);

      verify(uploadedContentRepository, times(2)).getBinaryResourceModelByResourceId(anyLong());
      verify(uploadedContentRepository, times(2)).deleteBinaryResourceModelByResourceId(anyLong());
      verify(awsService, times(2)).deleteObject(null, FILE_NAME);
   }

   @Test
   void getAudioBinaryDataWithRange_whenValidRange_thenReturnPartialContent() throws IOException {
      S3ObjectInputStream s3ObjectInputStream = mock(S3ObjectInputStream.class);
      S3Object s3Object = mock(S3Object.class);
      when(uploadedContentRepository.getBinaryResourceModelByResourceId(ID)).thenReturn(RESOURCE_MODEL);
      when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);
      when(s3ObjectInputStream.readNBytes(new byte[1], 0, 1)).thenReturn(0);
      when(awsService.getObject(null, "file_name")).thenReturn(s3Object);

      ResponseEntity<byte[]> audioBinaryDataWithRange = resourceService.getAudioBinaryDataWithRange(ID, VALID_RANGE);

      assertEquals(HttpStatus.PARTIAL_CONTENT, audioBinaryDataWithRange.getStatusCode());
      verify(awsService).getObject(null, "file_name");
      verify(uploadedContentRepository).getBinaryResourceModelByResourceId(ID);
   }

   @Test
   void getAudioBinaryDataWithRange_whenInvalidSizeRange_thenThrowException() throws IOException {
      try {
         resourceService.getAudioBinaryDataWithRange(ID, INVALID_SIZE_RANGE);
         fail("Exception expected");
      } catch (ResponseStatusException e) {
         assertEquals(BAD_REQUEST, e.getMessage());
      }
   }

   @Test
   void getAudioBinaryDataWithRange_whenInvalidRange_thenThrowException() throws IOException {
      try {
         resourceService.getAudioBinaryDataWithRange(ID, INVALID_RANGE);
         fail("Exception expected");
      } catch (ResponseStatusException e) {
         assertEquals(BAD_REQUEST, e.getMessage());
      }
   }
}