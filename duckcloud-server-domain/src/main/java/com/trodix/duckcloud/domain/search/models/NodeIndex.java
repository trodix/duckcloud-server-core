package com.trodix.duckcloud.domain.search.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "node")
public class NodeIndex {

    @Id
    private Long dbId;

    private String type;

    @MultiField(mainField = @Field(type = FieldType.Text))
    private List<String> tags;

    @MultiField(mainField = @Field(type = FieldType.Flattened))
    private Map<String, Serializable> properties;

    @Field(type = FieldType.Text, name = "cm:textContent")
    private String filecontent;

}