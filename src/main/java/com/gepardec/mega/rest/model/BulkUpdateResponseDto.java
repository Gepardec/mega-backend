package com.gepardec.mega.rest.model;

import java.util.List;

public record BulkUpdateResponseDto(
        String message,
        List<Integer> location
) {
}
