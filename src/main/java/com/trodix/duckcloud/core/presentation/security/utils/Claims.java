package com.trodix.duckcloud.core.presentation.security.utils;

public enum Claims {
    EMAIL("email"),
    NAME("name"),;

    public final String value;

    private Claims(final String value) {
        this.value = value;
    }

    @Override 
    public String toString() { 
        return this.value; 
    }
}
