package com.trodix.duckcloud.domain.models;

public class ContentModel {
    public static final String prefix = "cm";
    private static final String separator = ":";

    private static String getName(String model) {
        return prefix + separator + model;
    }

    public static final String TYPE_DIRECTORY = getName("directory");

    public static final String TYPE_CONTENT = getName("content");

    public static final String PROP_BUCKET = getName("bucket");

    public static final String PROP_CONTENT_LOCATION = getName("contentLocation");

    public static final String PROP_CONTENT_SIZE = getName("contentSize");

    public static final String PROP_NAME = getName("name");

    public static final String PROP_CREATED_AT = getName("createdAt");

    public static final String PROP_CREATED_BY = getName("createdBy");

    public static final String PROP_CREATED_BY_DISPLAY_NAME = getName("createdByDisplayName");

    public static final String PROP_MODIFIED_AT = getName("modifiedAt");

    public static final String PROP_MODIFIED_BY = getName("modifiedBy");

    public static final String PROP_MODIFIED_BY_DISPLAY_NAME = getName("modifiedByDisplayName");

}
