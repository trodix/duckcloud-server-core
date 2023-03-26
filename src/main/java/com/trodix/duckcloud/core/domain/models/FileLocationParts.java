package com.trodix.duckcloud.core.domain.models;

import lombok.*;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class FileLocationParts {

    private String bucket;

    private String path;

}
