package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ZepCategory(String name, Map<String, String> description) {
}
