package com.trodix.duckcloud.presentation.controllers;

import com.trodix.duckcloud.domain.search.models.NodeIndex;
import com.trodix.duckcloud.domain.search.models.SearchQuery;
import com.trodix.duckcloud.domain.search.models.SearchResult;
import com.trodix.duckcloud.domain.search.services.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "Search indexed nodes by metadata (elasticsearch)")
    @PostMapping("")
    public SearchResult<NodeIndex> searchNodes(@RequestBody final SearchQuery searchRequest, @RequestParam(defaultValue = "0") final Integer limit) {
        final List<NodeIndex> result = searchService.findNodeByFieldContaining(searchRequest.getTerm(), searchRequest.getValue(), limit);
        return new SearchResult<>(result.size(), result);
    }

}