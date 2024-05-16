package com.example.notes.api.serializer;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NoteBodySerializer(@JsonProperty String body){}
