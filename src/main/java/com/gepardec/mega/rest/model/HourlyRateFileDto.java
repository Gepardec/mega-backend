package com.gepardec.mega.rest.model;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import java.io.InputStream;

public class HourlyRateFileDto{
        @FormParam("file")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        private InputStream file;

    public InputStream getFile() {
        return file;
    }
}
