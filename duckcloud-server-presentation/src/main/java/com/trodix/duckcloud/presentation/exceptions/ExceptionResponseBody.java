package com.trodix.duckcloud.presentation.exceptions;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ExceptionResponseBody {

    private OffsetDateTime timestamp;

    private String message;

}
