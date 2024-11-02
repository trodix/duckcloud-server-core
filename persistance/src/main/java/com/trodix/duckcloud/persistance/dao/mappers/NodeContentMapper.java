package com.trodix.duckcloud.persistance.dao.mappers;

import com.trodix.duckcloud.persistance.entities.NodeContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface NodeContentMapper {

    List<NodeContent> findByNodeId(@Param("nodeId") Long nodeId);

    Optional<NodeContent> findByNodeIdAndVersion(@Param("nodeId") Long nodeId, @Param("contentVersion") float contentVersion);

    void insert(NodeContent nodeContent);

    void deleteAllByNodeId(@Param("nodeId") Long nodeId);

    void deleteByNodeIdAndVersion(@Param("nodeId") Long nodeId, @Param("contentVersion") float contentVersion);

}
