package com.trodix.duckcloud.persistance.entities;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class Property {

    private Long id;

    private Long nodeId;

    private String propertyName;

    private String stringVal;

    private Long longVal;

    private Double doubleVal;

    private OffsetDateTime dateVal;

}
