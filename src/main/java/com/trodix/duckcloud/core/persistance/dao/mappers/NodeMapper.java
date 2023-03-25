package com.trodix.duckcloud.core.persistance.dao.mappers;

import com.trodix.duckcloud.core.persistance.entities.Node;
import com.trodix.duckcloud.core.persistance.entities.TreeNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NodeMapper {

    Node findOne(Long id);

    List<Node> findAll();

    List<TreeNode> findTreeNodesByParentId(Long parentId);

    void insert(Node node);

    void insertNodeTagAssociation(@Param("nodeId") Long nodeId, @Param("tagId") Long tagId);

    void update(Node node);

    void delete(Long id);

    void deleteAllNodeTagAssociations(Long id);

    void deleteNodeTagAssociation(Long nodeId, Long tagId);

}
