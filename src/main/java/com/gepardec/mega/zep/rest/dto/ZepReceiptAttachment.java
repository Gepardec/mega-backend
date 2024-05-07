package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ZepReceiptAttachment (
        String fileContent
) {
    public static Builder builder() {return Builder.aZepReceiptAttachment();}

    @JsonCreator
    public ZepReceiptAttachment(Builder builder) {
        this (
                builder.fileContent
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        @JsonProperty("file_contents")
        private String fileContent;

        private Builder() {
        }

        public static Builder aZepReceiptAttachment() {return new Builder();}


        public Builder fileContent(String fileContent) {
            this.fileContent = fileContent;
            return this;
        }

        public ZepReceiptAttachment build() { return new ZepReceiptAttachment(this); }
    }
}
