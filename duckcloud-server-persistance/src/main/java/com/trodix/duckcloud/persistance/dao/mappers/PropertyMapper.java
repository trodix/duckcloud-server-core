package com.trodix.duckcloud.persistance.dao.mappers;

import com.trodix.duckcloud.persistance.entities.Property;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PropertyMapper {

    Property findOne(Long id);

    List<Property> findAll();

    void insert(Property property);

    void update(Property property);

    void delete(Long id);

    List<Property> findAllByNodeId(Long id);

    void deleteByNodeId(Long id);
}
