package com.trodix.duckcloud.persistance.dao.mappers;

import com.trodix.duckcloud.persistance.entities.Node;
import com.trodix.duckcloud.persistance.entities.TreeNode;
import com.trodix.duckcloud.security.annotations.FilterAuthorized;
import com.trodix.duckcloud.security.models.PermissionType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Optional;

@Mapper
public interface NodeMapper {

    Optional<Node> findOne(Long id);

    List<Node> findAll();

    List<TreeNode> findTreeNodesByParentId(@Param("parentId") Long parentId);

    List<TreeNode> findRecursiveNodeParents(@Param("nodeId") Long nodeId);

    List<Node> findAllByNodeId(@Param("ids") List<Long> ids);

    @FilterAuthorized(permissionType = PermissionType.READ)
    List<Node> findAllByParentId(Long parentId);

    List<Node> findAllTypeContentPageable(RowBounds rowbounds);

    long count();

    void insert(Node node);

    void insertNodeTagAssociation(@Param("nodeId") Long nodeId, @Param("tagId") Long tagId);

    void update(Node node);

    void delete(Long id);

    void deleteAllNodeTagAssociations(Long id);

    void deleteNodeTagAssociation(Long nodeId, Long tagId);

}
