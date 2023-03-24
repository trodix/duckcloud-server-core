package com.trodix.duckcloud.core.presentation.dto.mappers;

import com.trodix.duckcloud.core.persistance.entities.Property;
import com.trodix.duckcloud.core.persistance.entities.Type;
import com.trodix.duckcloud.core.presentation.dto.requests.TypeRequest;
import com.trodix.duckcloud.core.presentation.dto.responses.PropertyResponse;
import org.mapstruct.Mapper;

import java.io.Serializable;
import java.util.List;

@Mapper
public interface PropertyMapper {

    Type toEntity(TypeRequest request);

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
