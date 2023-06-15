package org.resource.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Component;

import static org.resource.exceptions.ExceptionMessage.INVALID_FILE_FORMAT;
import static org.resource.exceptions.ExceptionMessage.KAFKA_SERVER_UNAVAILABLE;
import static org.resource.exceptions.ExceptionMessage.RESOURCE_NOT_FOUND;
import static org.resource.exceptions.ExceptionMessage.UNABLE_TO_MAP_ENTITY;
import static org.resource.exceptions.ExceptionMessage.UNABLE_TO_MOVE_DATA_FROM_STAGING_TO_PERMANENT_STORAGE;
import static org.resource.exceptions.ExceptionMessage.UNABLE_TO_RETRIEVE_STORAGE_OBJECT;
import static org.resource.exceptions.ExceptionMessage.UNABLE_TO_UPLOAD_DATA_TO_BUCKET;

@Component
public class ExceptionFactory {

    public APIException invalidFileFormatException() {
        return new APIException(HttpStatus.BAD_REQUEST, INVALID_FILE_FORMAT);
    }

    public APIException audioBinaryDataNotFound() {
        return new APIException(HttpStatus.NOT_FOUND, RESOURCE_NOT_FOUND);
    }

    public APIException unableToUploadDataToBucket(Exception ex) {
        APIException apiException = new APIException(HttpStatus.BAD_REQUEST, UNABLE_TO_UPLOAD_DATA_TO_BUCKET);
        apiException.setDetail(ex.getMessage());
        return apiException;
    }

    public APIException unableToMoveDataFromStagingToPermanentStorage(Exception ex) {
        APIException apiException = new APIException(HttpStatus.BAD_REQUEST, UNABLE_TO_MOVE_DATA_FROM_STAGING_TO_PERMANENT_STORAGE);
        apiException.setDetail(ex.getMessage());
        return apiException;
    }

    public APIException unableToRetrieveStorageObject(Exception ex) {
        APIException apiException = new APIException(HttpStatus.BAD_REQUEST, UNABLE_TO_RETRIEVE_STORAGE_OBJECT);
        apiException.setDetail(ex.getMessage());
        return apiException;
    }

    public APIException unableToMapEntity(Exception ex) {
        APIException apiException = new APIException(HttpStatus.BAD_REQUEST, UNABLE_TO_MAP_ENTITY);
        apiException.setDetail(ex.getMessage());
        return apiException;
    }

    public APIException invalidDataRange(String message) {
        APIException apiException = new APIException(HttpStatus.BAD_REQUEST, UNABLE_TO_MAP_ENTITY);
        apiException.setDetail(message);
        return apiException;
    }

    public APIException brokerServerUnavailable(KafkaException e) {
        APIException apiException = new APIException(HttpStatus.INTERNAL_SERVER_ERROR, KAFKA_SERVER_UNAVAILABLE);
        apiException.setDetail(e.getMessage());
        return apiException;
    }
}
