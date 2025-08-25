package com.gepardec.mega.rest.model;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import java.io.InputStream;

public record HourlyRateFileDto(
        @FormParam("file")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        InputStream file
) {
}
