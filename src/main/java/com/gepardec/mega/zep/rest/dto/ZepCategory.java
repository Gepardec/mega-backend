package com.gepardec.mega.zep.rest.dto;

import java.util.Map;

public record ZepCategory(String name, Map<String, String> description) {
}
