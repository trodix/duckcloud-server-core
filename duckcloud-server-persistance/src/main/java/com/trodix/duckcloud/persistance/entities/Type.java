package com.trodix.duckcloud.persistance.entities;

import lombok.Data;

@Data
public class Type {

    private long id;
    private String name;

    public String toString() {
        return this.name;
    }

}
