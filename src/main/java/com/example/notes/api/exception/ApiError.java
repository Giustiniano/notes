package com.example.notes.api.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApiError {
    @JsonProperty
    private String status;
    @JsonProperty
    private String title;
    @JsonProperty
    private String detailMessage;
}
