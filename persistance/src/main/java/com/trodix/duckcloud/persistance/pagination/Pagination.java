package com.trodix.duckcloud.persistance.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pagination {

    private int offset = 0;
    private int pageSize = 50;

}
