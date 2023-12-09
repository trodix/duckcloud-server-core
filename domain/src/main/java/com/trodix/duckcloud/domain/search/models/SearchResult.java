package com.trodix.duckcloud.domain.search.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SearchResult<T> {

    private final Integer resultCount;

    private final List<T> items;

}
