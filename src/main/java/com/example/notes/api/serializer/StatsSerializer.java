package com.example.notes.api.serializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
@Data
@AllArgsConstructor
public class StatsSerializer {
    @JsonProperty
    private Map<String, Integer> wordCount;
}
