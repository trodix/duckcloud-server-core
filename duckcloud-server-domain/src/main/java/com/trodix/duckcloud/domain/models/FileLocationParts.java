package com.trodix.duckcloud.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class FileLocationParts {

    private String bucket;

    private String path;

}
