package com.trodix.duckcloud.domain.utils;

import com.trodix.duckcloud.domain.models.ContentModel;
import com.trodix.duckcloud.persistance.entities.Node;

public class ModelUtils {

    public static boolean isContentType(Node node) {
        if (node.getType() == null) {
            return false;
        }
        return ContentModel.TYPE_CONTENT.equals(node.getType().getName());
    }

    public static boolean isDirectoryType(Node node) {
        if (node.getType() == null) {
            return false;
        }
        return ContentModel.TYPE_DIRECTORY.equals(node.getType().getName());
    }

}
