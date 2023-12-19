package com.trodix.duckcloud.persistance.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class PaginationResult<T extends Collection> {

    private int offset;
    private int pageSize;
    private int total;
    private T entries;

}
