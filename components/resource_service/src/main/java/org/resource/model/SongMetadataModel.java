package org.resource.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SongMetadataModel {

    private final Long id;
    private final Long resourceId;
    private final String year;
    private final String name;
    private final String artist;
    private final String album;
    private final String length;

}
