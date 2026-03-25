package com.gepardec.mega.rest.model;

import java.util.List;

public record BulkUpdateErrorResponseDto(
        String message,
        List<Integer> location
) {
}
