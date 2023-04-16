package com.trodix.duckcloud.presentation.dto.mappers;

import com.trodix.duckcloud.persistance.entities.Property;
import com.trodix.duckcloud.presentation.dto.requests.PropertyRequest;
import com.trodix.duckcloud.presentation.dto.responses.PropertyResponse;
import org.mapstruct.Mapper;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface PropertyMapper {

    default List<Property> toEntity(Map<String, Serializable> map) {
        List<PropertyRequest> propertiRequestList = new ArrayList<>();

        for (Map.Entry<String, Serializable> entry : map.entrySet()) {
            PropertyRequest propertyRequest = new PropertyRequest();
            propertyRequest.setKey(entry.getKey());
            propertyRequest.setValue(entry.getValue());

            propertiRequestList.add(propertyRequest);
        }

        List<Property> result = propertiRequestList.stream().map(p -> toEntity(p)).collect(Collectors.toList());

        return result;
    }

    default Property toEntity(PropertyRequest request) {
        Property property = new Property();
        property.setPropertyName(request.getKey());

        if (request.getValue() instanceof String) {
            property.setStringVal((String) request.getValue());
        }
        else if (request.getValue() instanceof Integer) {
            property.setLongVal(Long.valueOf(request.getValue().toString()));
        } else if (request.getValue() instanceof Long) {
            property.setLongVal((Long) request.getValue());
        }
        else if (request.getValue() instanceof Double) {
            property.setDoubleVal((Double) request.getValue());
        }
        else if (request.getValue() instanceof OffsetDateTime) {
            property.setDateVal((OffsetDateTime) request.getValue());
        } else {
            // TODO add serializable property value type
            throw new IllegalArgumentException("Value type not supported : " + request.getValue());
        }

        return property;
    }

    default PropertyResponse toDto(Property property) {
        PropertyResponse target = new PropertyResponse();
        target.setKey(property.getPropertyName());

        if (property.getStringVal() != null) {
            target.setValue(property.getStringVal());
        } else if (property.getLongVal() != null) {
            target.setValue(property.getLongVal());
        } else if (property.getDoubleVal() != null) {
            target.setValue(property.getDoubleVal());
        } else if (property.getDateVal() != null) {
            target.setValue(property.getDateVal());
        }

        return target;
    }

    List<PropertyResponse> toDto(List<Property> properties);

}
