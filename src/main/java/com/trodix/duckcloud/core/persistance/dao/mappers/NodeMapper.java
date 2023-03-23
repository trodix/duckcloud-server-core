package com.trodix.duckcloud.core.persistance.dao.mappers;

import com.trodix.duckcloud.core.persistance.entities.Node;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NodeMapper {

    void insertNode(Node node);

}
