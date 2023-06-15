package org.resource.service;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Properties {

    private static final String DEFAULT_STORAGE_URL = "http://localhost:4566";
    private static final String DEFAULT_STORAGE_SERVICE_URL = "http://localhost:8085/storages/type";

    @Value("${s3.storage.url}")
    private String storageUrl;
    @Value("${storage.service.url}")
    private String storageServiceUrl;

    public String getStorageUrl() {
        return storageUrl == null ? DEFAULT_STORAGE_URL : storageUrl;
    }

    public String getStorageServiceUrl() {
        return storageServiceUrl == null ? DEFAULT_STORAGE_SERVICE_URL : storageServiceUrl;
    }

}


