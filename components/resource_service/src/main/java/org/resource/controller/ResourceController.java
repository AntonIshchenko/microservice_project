package org.resource.controller;

import lombok.RequiredArgsConstructor;
import org.resource.model.BinaryResourceModel;
import org.resource.model.StorageType;
import org.resource.service.ResourceService;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/resources")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping(path = "/echo", produces = MediaType.APPLICATION_JSON_VALUE)
    public String echo() {
        return resourceService.echo();
    }

    @PostMapping(path = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long uploadNewResource(@RequestPart MultipartFile data) {
        return resourceService.uploadNewResource(data);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getAudioBinaryData(@PathVariable Long id,
                                  @RequestParam(required = false, defaultValue = "") List<Integer> range,
                                  @RequestParam(required = false, defaultValue = "PERMANENT") StorageType storageType) {
        return resourceService.getAudioData(id, range, storageType);
    }

    @GetMapping(path = "/{id}/model")
    public BinaryResourceModel getModelById(@PathVariable Long id) {
        return resourceService.getResourceModelByID(id);
    }

    @DeleteMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Long> deleteSongs(@RequestParam List<Long> id) {
        return resourceService.deleteSongs(id, StorageType.PERMANENT);
    }

    @KafkaListener(id = "entityJSONListener",
            containerFactory = "jsonEntityConsumerFactory",
            topics = "storage-transfer-to-permanent.entityJson",
            groupId = "search.entity-json-consumer")
    public void handleMessage(String value) {
        resourceService.transferFormStagingToPermanent(value);
    }
}
