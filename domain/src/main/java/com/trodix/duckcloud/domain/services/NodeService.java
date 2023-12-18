package com.trodix.duckcloud.domain.services;

import com.trodix.casbinserver.client.api.v1.EnforcerApi;
import com.trodix.casbinserver.models.PermissionType;
import com.trodix.duckcloud.domain.models.*;
import com.trodix.duckcloud.domain.search.models.NodeIndex;
import com.trodix.duckcloud.domain.search.services.NodeIndexerService;
import com.trodix.duckcloud.domain.utils.ModelUtils;
import com.trodix.duckcloud.domain.utils.StorageUtils;
import com.trodix.duckcloud.persistance.dao.NodeManager;
import com.trodix.duckcloud.persistance.entities.*;
import com.trodix.duckcloud.persistance.utils.NodeUtils;
import com.trodix.duckcloud.security.services.AuthenticationService;
import io.minio.ObjectWriteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NodeService {

    private final NodeManager nodeManager;

    private final StorageService storageService;

    private final NodeContentService nodeContentService;

    private final NodeIndexerService nodeIndexerService;

    private final AuthenticationService authenticationService;

    private final EnforcerApi enforcer;

    public Optional<Node> getOne(Long id) {
        return nodeManager.findOne(id);
    }

    public List<Node> getAll() {
        return nodeManager.findAll();
    }

    public List<Node> getChildren(Long id) {
        return nodeManager.findAllByParentId(id);
    }

    public List<NodeWithPath> getChildrenWithPath(Long id) {
        List<String> path = nodeManager.buildTreeFromParent(id).stream().map(tn -> tn.getNodePath()).toList().get(0);
        List<Node> parentNodes = nodeManager.findAllByNodeId(path.stream().map(p -> Long.valueOf(p)).toList());
        List<NodePath> pathObj = parentNodes.stream().map(n -> new NodePath(n.getId(), NodeUtils.getProperty(n.getProperties(), ContentModel.PROP_NAME).get().getStringVal())).toList();
        return getChildren(id).stream().map(n -> {
            NodeWithPath nodeWithPath = new NodeWithPath();
            nodeWithPath.setId(n.getId());
            nodeWithPath.setParentId(n.getParentId());
            nodeWithPath.setType(n.getType());
            nodeWithPath.setTags(n.getTags());
            nodeWithPath.setProperties(n.getProperties());
            nodeWithPath.setPath(pathObj);

            return nodeWithPath;
        }).toList();
    }

    public Optional<NodeWithPath> getOneNodeWithRecursiveParents(Long nodeId) {
        List<TreeNode> treeNodeList = nodeManager.buildTreeWithRecursiveParents(nodeId);
        TreeNode treeNode = treeNodeList.stream().findFirst().orElseThrow();
        Node n = getOne(treeNode.getNodeId()).orElseThrow();

        NodeWithPath nodeWithPath = new NodeWithPath();
        nodeWithPath.setId(n.getId());
        nodeWithPath.setParentId(n.getParentId());
        nodeWithPath.setType(n.getType());
        nodeWithPath.setTags(n.getTags());
        nodeWithPath.setProperties(n.getProperties());

        List<Node> parents = nodeManager.findAllByNodeId(treeNode.getNodePath().stream().map(i -> Long.valueOf(i)).toList());

        List<NodePath> pathObj = parents.stream().map(parent ->
                new NodePath(parent.getId(), NodeUtils.getProperty(parent.getProperties(), ContentModel.PROP_NAME).map(p -> p.getStringVal()).orElse(""))
        ).toList();

        nodeWithPath.setPath(pathObj);

        return Optional.ofNullable(nodeWithPath);
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
                        nodeTree.setName(NodeUtils.getProperty(node.getProperties(), ContentModel.PROP_NAME).map(p -> p.getStringVal()).orElse(""));

                        nodeTree.setType(node.getType().getName());
                        nodeTree.setTags(node.getTags());
                        nodeTree.setProperties(node.getProperties());
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
        if (!hasName(node)) {
            throw new IllegalArgumentException("Property " + ContentModel.PROP_NAME + " was not found");
        }
        setCreatedAuthorProperties(node);
        nodeManager.create(node);

        indexNode(node);
    }

    public void indexNode(Node node) {
        try {
            NodeIndex nodeIndex = nodeIndexerService.buildIndex(node);
            nodeIndexerService.createNodeIndex(nodeIndex);
        } catch (RuntimeException e) {
            log.error("Error while indexing node with id {}", node.getId(), e);
        }
    }

    public void createNodeWithContent(Node node, FileStoreMetadata fileStoreMetadata, byte[] file) {

        if (!ModelUtils.isContentType(node)) {
            throw new IllegalArgumentException("Node should be of type " + ContentModel.TYPE_CONTENT);
        }

        createContentForNode(node, fileStoreMetadata, file);

        create(node);
    }

    private void createContentForNode(Node node, FileStoreMetadata fileStoreMetadata, byte[] file) {
        ObjectWriteResponse storedFileResponse = storageService.uploadFile(fileStoreMetadata, file);
        String objectBucket = storedFileResponse.bucket();
        String objectPath = storedFileResponse.object();
        String fullPath = objectBucket + ":" + objectPath;
        log.debug("File uploaded at path: {}", fullPath);

        Property contentLocation = new Property();
        contentLocation.setPropertyName(ContentModel.PROP_CONTENT_LOCATION);
        contentLocation.setStringVal(fullPath);
        NodeUtils.addProperty(node, contentLocation);

        Property contentSize = new Property();
        contentSize.setPropertyName(ContentModel.PROP_CONTENT_SIZE);
        contentSize.setLongVal(Long.valueOf(file.length));
        NodeUtils.addProperty(node, contentSize);

        Property fileName = new Property();
        fileName.setPropertyName(ContentModel.PROP_NAME);
        fileName.setStringVal(fileStoreMetadata.getOriginalName());
        NodeUtils.addProperty(node, fileName);
    }

    public void updateNodeContent(Node node, FileStoreMetadata fileStoreMetadata, byte[] file) {

        if (!ModelUtils.isContentType(node)) {
            throw new IllegalArgumentException("Node should be of type " + ContentModel.TYPE_CONTENT);
        }

        if (!hasName(node)) {
            throw new IllegalArgumentException("Property " + ContentModel.PROP_NAME + " was not found");
        }

        Optional<Property> optionalContentLocation = NodeUtils.getProperty(node.getProperties(), ContentModel.PROP_CONTENT_LOCATION);

        if (optionalContentLocation.isPresent()) {
            String contentLocation = optionalContentLocation.get().getStringVal();
            FileLocationParts contentLocationParts = StorageUtils.getFileLocationParts(contentLocation);
            storageService.deleteFile(contentLocationParts.getBucket(), contentLocationParts.getPath());
        }

        createContentForNode(node, fileStoreMetadata, file);
        update(node);
    }

    public void update(Node node) {
        if (!hasName(node)) {
            throw new IllegalArgumentException("Property " + ContentModel.PROP_NAME + " was not found");
        }
        setModifiedAuthorProperties(node);
        // merge updated properties with existing properties
        Node existingNode = getOne(node.getId()).orElseThrow(() -> new IllegalArgumentException("Trying to update a node not found in database"));

        // merge type
        if (node.getType() != null && !node.getType().equals(existingNode.getType())) {
            existingNode.setType(node.getType());
        }

        // merge parentId (move)
        if (node.getParentId() != null && !node.getParentId().equals(existingNode.getParentId())) {
            existingNode.setParentId(node.getParentId());
        }

        // TODO merge tags

        // merge properties
        NodeUtils.addProperties(existingNode, node.getProperties());

        nodeManager.update(existingNode);
        indexNode(existingNode);
    }

    public void setCreatedAuthorProperties(Node node) {
        final Property createdAtProp = new Property();
        createdAtProp.setPropertyName(ContentModel.PROP_CREATED_AT);
        createdAtProp.setDateVal(OffsetDateTime.now());

        final Property createdByProp = new Property();
        createdByProp.setPropertyName(ContentModel.PROP_CREATED_BY);
        createdByProp.setStringVal(authenticationService.getUserId());

        final Property createdByDisplayNameProp = new Property();
        createdByDisplayNameProp.setPropertyName(ContentModel.PROP_CREATED_BY_DISPLAY_NAME);
        createdByDisplayNameProp.setStringVal(authenticationService.getName());

        NodeUtils.addProperty(node, createdAtProp);
        NodeUtils.addProperty(node, createdByProp);
        NodeUtils.addProperty(node, createdByDisplayNameProp);
    }

    public void setModifiedAuthorProperties(Node node) {

        String userId = authenticationService.getUserId();
        String userName = authenticationService.getName();

        userId = userId == null ? AuthenticationService.DEFAULT_USER : userId;
        userName = userName == null ? AuthenticationService.DEFAULT_USER : userName;

        final Property modifiedAtProp = new Property();
        modifiedAtProp.setPropertyName(ContentModel.PROP_MODIFIED_AT);
        modifiedAtProp.setDateVal(OffsetDateTime.now());

        final Property modifiedByProp = new Property();
        modifiedByProp.setPropertyName(ContentModel.PROP_MODIFIED_BY);
        modifiedByProp.setStringVal(userId);

        final Property modifiedByDisplayNameProp = new Property();
        modifiedByDisplayNameProp.setPropertyName(ContentModel.PROP_MODIFIED_BY_DISPLAY_NAME);
        modifiedByDisplayNameProp.setStringVal(userName);

        NodeUtils.addProperty(node, modifiedAtProp);
        NodeUtils.addProperty(node, modifiedByProp);
        NodeUtils.addProperty(node, modifiedByDisplayNameProp);
    }

    public void delete(Long id) {

        Node node = getOne(id).orElseThrow(() -> new IllegalArgumentException("Node with id " + id + " not found"));

        if (ModelUtils.isContentType(node)) {
            Optional<Property> contentLocation = NodeUtils.getProperty(node.getProperties(), ContentModel.PROP_CONTENT_LOCATION);

            if (contentLocation.isPresent()) {
                FileLocationParts fileLocationParts = StorageUtils.getFileLocationParts(contentLocation.get().getStringVal());
                storageService.deleteFile(fileLocationParts.getBucket(), fileLocationParts.getPath());
            }

        }

        nodeManager.delete(id);

        enforcer.removeFilteredPolicy(1, String.format("feature:node:%s", id));

        enforcer.removeFilteredGroupingPolicy(1, String.format("role:node:%s:%s", id, PermissionType.READ));
        enforcer.removeFilteredGroupingPolicy(1, String.format("role:node:%s:%s", id, PermissionType.WRITE));
        enforcer.removeFilteredGroupingPolicy(1, String.format("role:node:%s:%s", id, PermissionType.DELETE));

        try {
            nodeIndexerService.deleteNodeIndex(node.getId());
        } catch (RuntimeException e) {
            log.error("Error while trying to remove index for nodeId {}", node.getId(), e);
        }

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
        if (!ModelUtils.isContentType(node)) {
            throw new IllegalArgumentException("Node must be of type " + ContentModel.TYPE_CONTENT);
        }

        Property contentLocation = NodeUtils.getProperty(node.getProperties(), ContentModel.PROP_CONTENT_LOCATION)
                .orElseThrow(() -> new IllegalStateException(
                        "Property " + ContentModel.PROP_CONTENT_LOCATION + " not found on node id " + node.getId()));

        FileLocationParts fileLocationParts = StorageUtils.getFileLocationParts(contentLocation.getStringVal());

        byte[] file = storageService.getFile(fileLocationParts.getBucket(), fileLocationParts.getPath());

        return file;
    }

    private boolean hasName(Node node) {
        return !StringUtils.isBlank(NodeUtils.getProperty(node.getProperties(), ContentModel.PROP_NAME).orElse(new Property()).getStringVal());
    }

}
