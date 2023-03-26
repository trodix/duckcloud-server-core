package com.trodix.duckcloud.core.domain.services;

import com.trodix.duckcloud.core.domain.models.FileStoreMetadata;
import com.trodix.duckcloud.core.persistance.dao.NodeManager;
import com.trodix.duckcloud.core.persistance.entities.Node;
import com.trodix.duckcloud.core.persistance.entities.Property;
import com.trodix.duckcloud.core.persistance.entities.TreeNode;
import com.trodix.duckcloud.core.domain.models.ContentModel;
import com.trodix.duckcloud.core.domain.models.FileLocationParts;
import com.trodix.duckcloud.core.utils.NodeUtils;
import com.trodix.duckcloud.core.utils.StorageUtils;
import io.minio.ObjectWriteResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class NodeService {

    private final NodeManager nodeManager;

    private final StorageService storageService;

    public Optional<Node> getOne(Long id) {
        return nodeManager.findOne(id);
    }

    public List<Node> getAll() {
        return nodeManager.findAll();
    }

    public List<TreeNode> buildTreeFromParent(Long parentId) {
        List<TreeNode> treeNodeList = nodeManager.buildTreeFromParent(parentId);

        // associate node properties to tree items
        List<Long> nodeIdsInTree = treeNodeList.stream().map(n -> n.getNodeId()).toList();
        List<Node> nodeList = nodeManager.findAllByNodeId(nodeIdsInTree);

        nodeList.forEach(node -> {
            treeNodeList.stream()
                    .filter(treeNode -> treeNode.getNodeId() == node.getId())
                    .findAny()
                    .ifPresent(nodeTree -> {
                        nodeTree.setName(
                                node.getProperties().stream()
                                        .filter(p -> p.getPropertyName().equals(ContentModel.PROP_NAME)).findAny().get().getStringVal());

                        nodeTree.setType(node.getType().getName());
                    });
        });

        // build nested tree
        final List<TreeNode> copyList = new ArrayList<>(treeNodeList);

        copyList.forEach(element -> {
            treeNodeList
                    .stream()
                    .filter(parent -> parent.getNodeId() == element.getParentId() && parent.getNodeId() != element.getNodeId())
                    .findAny()
                    .ifPresent(parent -> {
                        parent.getChildren().add(element);
                    });
        });

        treeNodeList.subList(1, treeNodeList.size()).clear();

        return treeNodeList;
    }

    public void create(Node node) {
        nodeManager.create(node);
    }

    public void createNodeWithContent(Node node, FileStoreMetadata fileStoreMetadata, byte[] file) {

        if (!NodeUtils.isContentType(node)) {
            throw new IllegalArgumentException("Node should be of type " + ContentModel.TYPE_CONTENT);
        }

        ObjectWriteResponse storedFileResponse = storageService.uploadFile(fileStoreMetadata, file);
        String objectBucket = storedFileResponse.bucket();
        String objectPath = storedFileResponse.object();
        String fullPath = objectBucket + ":" + objectPath;
        log.debug("File uploaded at path: {}", fullPath);

        Property contentLocation = new Property();
        contentLocation.setPropertyName(ContentModel.PROP_CONTENT_LOCATION);
        contentLocation.setStringVal(fullPath);
        NodeUtils.addProperty(node, contentLocation);

        if (NodeUtils.getProperty(node.getProperties(), ContentModel.PROP_NAME).isEmpty()) {
            Property fileName = new Property();
            fileName.setPropertyName(ContentModel.PROP_NAME);
            fileName.setStringVal(fileStoreMetadata.getOriginalName());

            NodeUtils.addProperty(node, fileName);
        }

        create(node);
    }

    public void update(Node node) {
        nodeManager.update(node);
    }

    public void delete(Long id) {
        nodeManager.delete(id);
    }

    public FileStoreMetadata buildFileStoreMetadata(Node node, MultipartFile file) {
        FileStoreMetadata metadata = new FileStoreMetadata();
        metadata.setContentType(file.getContentType());

        Optional<Property> bucket = NodeUtils.getProperty(node.getProperties(), ContentModel.PROP_BUCKET);
        if (bucket.isPresent()) {
            metadata.setBucket(bucket.get().getStringVal());
        } else {
            metadata.setBucket(null);
        }

        metadata.setDirectoryPath(null);
        metadata.setUuid(null);
        metadata.setOriginalName(file.getOriginalFilename());

        return metadata;
    }

    public byte[] getFileContent(Node node) {
        if (!NodeUtils.isContentType(node)) {
            throw new IllegalArgumentException("Node must be of type " + ContentModel.TYPE_CONTENT);
        }

        Property contentLocation = NodeUtils.getProperty(node.getProperties(), ContentModel.PROP_CONTENT_LOCATION)
                .orElseThrow(() -> new IllegalStateException(
                        "Property " + ContentModel.PROP_CONTENT_LOCATION + " not found on node id " + node.getId()));

        FileLocationParts fileLocationParts = StorageUtils.getFileLocationParts(contentLocation.getStringVal());

        byte[] file = storageService.getFile(fileLocationParts.getBucket(), fileLocationParts.getPath());

        return file;
    }

}
