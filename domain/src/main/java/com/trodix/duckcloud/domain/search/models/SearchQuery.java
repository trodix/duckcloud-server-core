package com.trodix.duckcloud.domain.search.models;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchQuery {

    private String term;

    private Serializable value;

}
