package com.trodix.duckcloud.core.persistance.entities;

import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
public class Property {

    private Long id;

    private Long nodeId;

    private String propertyName;

    private String stringVal;

    private Serializable serializableVal;

    private Long longVal;

    private Double doubleVal;

    private OffsetDateTime dateVal;

}
