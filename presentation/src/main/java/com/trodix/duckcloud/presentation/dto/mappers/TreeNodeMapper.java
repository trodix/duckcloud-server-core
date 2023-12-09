package com.trodix.duckcloud.presentation.dto.mappers;

import com.trodix.duckcloud.persistance.entities.TreeNode;
import com.trodix.duckcloud.presentation.dto.responses.TreeNodeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {TypeMapper.class, TagMapper.class, PropertyMapper.class})
public interface TreeNodeMapper {

    @Mapping(source = "nodeId", target = "id")
    TreeNodeResponse toDto(TreeNode treeNode);

    List<TreeNodeResponse> toDto(List<TreeNode> treeNodeList);

}
