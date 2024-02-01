package org.resource.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.resource.exceptions.ExceptionFactory;
import org.resource.model.BinaryDataPage;
import org.resource.model.BinaryResourceModel;
import org.resource.model.SongMetadataModel;
import org.resource.model.StorageObject;
import org.resource.model.StorageType;
import org.resource.repository.UploadedContentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private static final String MEDIA_TYPE_AUDIO_MPEG = "audio/mpeg";

    private final AWSS3Service awsService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UploadedContentRepository uploadedContentRepository;
    private final ExceptionFactory exceptionFactory;

    public String echo() {
        StringBuilder result = new StringBuilder(awsService.getStorageUrl() + "\n" + "\n");
        List<Bucket> buckets = awsService.listBuckets();
        for (Bucket bucket : buckets) {
            result.append(bucket.getName()).append("\n");
        }
        CompletableFuture<SendResult<String, String>> sendResultListenableFuture = sendMessage(new BinaryResourceModel());
        try {
            result.append(sendResultListenableFuture.get().getProducerRecord().value());
        } catch (InterruptedException | ExecutionException e) {
            System.err.println(e.getMessage());
        }
        return result.toString();
    }

    @Transactional
    public Long uploadNewResource(MultipartFile data) {
        if (data.getContentType() == null || MEDIA_TYPE_AUDIO_MPEG.compareToIgnoreCase(data.getContentType()) != 0) {
            throw exceptionFactory.invalidFileFormatException();
        }

        long resourceId = System.currentTimeMillis();

        BinaryResourceModel resourceModel = new BinaryResourceModel(0, resourceId, data.getOriginalFilename(), StorageType.STAGING.name(), RequestMethod.POST);
        BinaryResourceModel ifExist = uploadedContentRepository.getBinaryResourceModelByName(data.getOriginalFilename());
        if (ifExist != null) {
            resourceModel.setResourceId(ifExist.getResourceId());
            sendMessage(resourceModel);
            return ifExist.getResourceId();
        }

        return uploadResourceModelToBucket(resourceModel, data);
    }

    private Long uploadResourceModelToBucket(BinaryResourceModel resourceModel, MultipartFile data) {
        BinaryResourceModel model = uploadedContentRepository.save(resourceModel);
        try {
            uploadDataToBucket(data.getOriginalFilename(), data.getInputStream(), StorageType.STAGING);
        } catch (Exception e) {
            throw exceptionFactory.unableToUploadDataToBucket(e);
        }
        sendMessage(model);
        return model.getResourceId();
    }

    private S3ObjectInputStream getAudioBinaryData(Long id, StorageType storageType) {
        try {
            return getAudioBinaryDataFromBucket(id, storageType);
        } catch (EntityNotFoundException | AmazonS3Exception | IOException e) {
            throw exceptionFactory.audioBinaryDataNotFound();
        }
    }

    private S3ObjectInputStream getAudioBinaryDataFromBucket(Long id, StorageType storageType)
            throws EntityNotFoundException, AmazonS3Exception, IOException {
        StorageObject storage = getStorageByType(storageType);
        BinaryResourceModel model = uploadedContentRepository.getBinaryResourceModelByResourceId(id);
        if (model == null) {
            throw exceptionFactory.audioBinaryDataNotFound();
        }
        S3ObjectInputStream dataFromBucket = getDataFromBucket(model, storage);
        model.setMethod(RequestMethod.GET);
        sendMessage(model);
        return dataFromBucket;
    }

    public BinaryResourceModel getResourceModelByID(Long id) {
        return Optional.ofNullable(uploadedContentRepository.getBinaryResourceModelByResourceId(id))
                .orElseThrow(exceptionFactory::audioBinaryDataNotFound);
    }

    @Transactional
    public List<Long> deleteSongs(List<Long> ids, StorageType storageType) {
        StorageObject storage = getStorageByType(storageType);
        ids.forEach(id -> deleteSongByIdAndStorage(id, storage));
        return ids;
    }

    private void deleteSongByIdAndStorage(Long id, StorageObject storage) {
        BinaryResourceModel model = uploadedContentRepository.getBinaryResourceModelByResourceId(id);
        uploadedContentRepository.deleteBinaryResourceModelByResourceId(model.getResourceId());
        deleteDataFromBucket(model.getName(), storage);
        model.setMethod(RequestMethod.DELETE);
        sendMessage(model);
    }

    private void uploadDataToBucket(String key, InputStream data, StorageType storageType) {
        List<String> keys = new ArrayList<>();

        StorageObject storage = getStorageByType(storageType);

        awsService.listObjects(storage.getBucket()).getObjectSummaries().forEach(e -> keys.add(e.getKey()));
        if (keys.contains(key)) {
            return;
        }
        awsService.putObject(storage.getBucket(), key, data);
    }

    private S3ObjectInputStream getDataFromBucket(BinaryResourceModel resourceModel, StorageObject storageObject) {
        S3Object s3Object = awsService.getObject(storageObject.getBucket(), resourceModel.getName());
        return s3Object.getObjectContent();
    }

    private void deleteDataFromBucket(String key, StorageObject storageObject) {
        awsService.deleteObject(storageObject.getBucket(), key);
    }

    private CompletableFuture<SendResult<String, String>> sendMessage(BinaryResourceModel model) {
        String messageKey = model.getClass().getSimpleName() + "|" + model.getName();
        try {
            String messageValue = objectMapper.writeValueAsString(model);
            return kafkaTemplate.send("resource-service.entityJson", messageKey, messageValue);
        } catch (JsonProcessingException e) {
            throw exceptionFactory.unableToMapEntity(e);
        } catch (KafkaException e) {
            throw exceptionFactory.brokerServerUnavailable(e);
        }
    }

    private StorageObject getStorageByType(StorageType storageType) {
        HttpUriRequest postRequest = RequestBuilder.get()
                .setUri(awsService.getStorageServiceUrl())
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .addParameter("storageType", storageType.name())
                .build();
        try {
            HttpResponse postResponse = HttpClientBuilder.create().build().execute(postRequest);
            String response = new String(postResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper.readValue(response, StorageObject.class);
        } catch (JsonProcessingException e) {
            throw exceptionFactory.unableToMapEntity(e);
        } catch (IOException e) {
            throw exceptionFactory.unableToRetrieveStorageObject(e);
        }
    }

    private void clearBucket(StorageObject storageObject) {
        List<String> keys = new ArrayList<>();
        awsService.listObjects(storageObject.getBucket()).getObjectSummaries().forEach(e -> keys.add(e.getKey()));
        keys.forEach(e -> awsService.deleteObject(storageObject.getBucket(), e));
    }

    private SongMetadataModel getSongMetadataModelDTO(String inputJson) {
        try {
            return objectMapper.readValue(inputJson, SongMetadataModel.class);
        } catch (Exception e) {
            throw exceptionFactory.unableToMoveDataFromStagingToPermanentStorage(e);
        }
    }

    @Transactional
    public void transferFormStagingToPermanent(String value) {
        Long resourceId = getSongMetadataModelDTO(value).getResourceId();
        StorageObject stagingStorage = getStorageByType(StorageType.STAGING);

        BinaryResourceModel binaryResourceModel = uploadedContentRepository.getBinaryResourceModelByResourceId(resourceId);
        InputStream binaryDataStream = retrieveAudioBinaryDataFromStagingStorage(binaryResourceModel, stagingStorage);

        uploadDataToBucket(binaryResourceModel.getName(), binaryDataStream, StorageType.PERMANENT);

        deleteDataFromBucket(binaryResourceModel.getName(), stagingStorage);

        binaryResourceModel.setStorageType(StorageType.PERMANENT.name());
        System.err.println(binaryResourceModel);
    }

    private InputStream retrieveAudioBinaryDataFromStagingStorage(BinaryResourceModel model, StorageObject storage) {
        try {
            S3ObjectInputStream audioBinaryData = getDataFromBucket(model, storage);
            return new ByteArrayInputStream(audioBinaryData.getDelegateStream().readAllBytes());
        } catch (IOException | AmazonServiceException e) {
            throw exceptionFactory.unableToMoveDataFromStagingToPermanentStorage(e);
        }
    }

    public List<Byte> getAudioData(Long id, Integer pageNumber, Integer pageSize, StorageType storageType) {
        try {
            List<Byte> bytes = Arrays.asList(ArrayUtils.toObject(getAudioBinaryData(id, storageType).readAllBytes()));
            return new BinaryDataPage(bytes, PageRequest.of(pageNumber, pageSize)).getContent();
        } catch (IOException e) {
            throw exceptionFactory.audioBinaryDataNotFound();
        }
    }

    public List<BinaryResourceModel> getAllResourceModels(Integer pageNumber, Integer pageSize) {
        return uploadedContentRepository.findAll(PageRequest.of(pageNumber, pageSize)).getContent();
    }
}