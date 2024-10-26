package com.trodix.duckcloud.security.utils;

public enum Claims {
    EMAIL("email"),
    NAME("name"),
    USERNAME("preferred_username"),;

    public final String value;

    Claims(final String value) {
        this.value = value;
    }

    @Override 
    public String toString() { 
        return this.value; 
    }
}
