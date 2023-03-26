package com.trodix.duckcloud.domain.services;

import com.trodix.duckcloud.persistance.dao.mappers.PropertyMapper;
import com.trodix.duckcloud.persistance.entities.Property;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyMapper propertyMapper;

    public void createProperty(Property property) {
        propertyMapper.insert(property);
    }

}
