package com.trodix.duckcloud.core.persistance.dao.mappers;

import com.trodix.duckcloud.core.persistance.entities.Property;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PropertyMapper {

    void insertProperty(Property property);

}
