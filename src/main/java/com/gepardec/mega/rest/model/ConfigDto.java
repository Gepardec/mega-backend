package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = ConfigDto.Builder.class)
public class ConfigDto {

    private final String excelUrl;

    private final String zepOrigin;

    private final String clientId;

    private final String issuer;

    private final String scope;

    private final String version;

    private final List<String> omMailAddresses;

    private final String subjectPrefix;

    private final String megaDashUrl;

    private ConfigDto(Builder builder) {
        this.excelUrl = builder.excelUrl;
        this.zepOrigin = builder.zepOrigin;
        this.clientId = builder.clientId;
        this.issuer = builder.issuer;
        this.scope = builder.scope;
        this.version = builder.version;
        this.omMailAddresses = builder.omMailAddresses;
        this.subjectPrefix = builder.subjectPrefix;
        this.megaDashUrl = builder.megaDashUrl;
    }

    public static Builder builder() {
        return Builder.aConfigDto();
    }

    public String getExcelUrl() {
        return excelUrl;
    }


    public String getZepOrigin() {
        return zepOrigin;
    }

    public String getClientId() {
        return clientId;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getScope() {
        return scope;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getOmMailAddresses() {
        return omMailAddresses;
    }

    public String getSubjectPrefix() {
        return subjectPrefix;
    }

    public String getMegaDashUrl() {
        return megaDashUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigDto configDto = (ConfigDto) o;
        return Objects.equals(getExcelUrl(), configDto.getExcelUrl()) && Objects.equals(getZepOrigin(), configDto.getZepOrigin()) && Objects.equals(getClientId(), configDto.getClientId()) && Objects.equals(getIssuer(), configDto.getIssuer()) && Objects.equals(getScope(), configDto.getScope()) && Objects.equals(getVersion(), configDto.getVersion()) && Objects.equals(getOmMailAddresses(), configDto.getOmMailAddresses()) && Objects.equals(getSubjectPrefix(), configDto.getSubjectPrefix()) && Objects.equals(getMegaDashUrl(), configDto.getMegaDashUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExcelUrl(), getZepOrigin(), getClientId(), getIssuer(), getScope(), getVersion(), getOmMailAddresses(), getSubjectPrefix(), getMegaDashUrl());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String excelUrl;
        private String zepOrigin;
        private String clientId;
        private String issuer;
        private String scope;
        private String version;
        private List<String> omMailAddresses;
        private String subjectPrefix;
        private String megaDashUrl;

        private Builder() {
        }

        public static Builder aConfigDto() {
            return new Builder();
        }

        public Builder excelUrl(String excelUrl) {
            this.excelUrl = excelUrl;
            return this;
        }

        public Builder zepOrigin(String zepOrigin) {
            this.zepOrigin = zepOrigin;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder omMailAddresses(List<String> omMailAddresses) {
            this.omMailAddresses = omMailAddresses;
            return this;
        }

        public Builder subjectPrefix(String subjectPrefix) {
            this.subjectPrefix = subjectPrefix;
            return this;
        }

        public Builder megaDashUrl(String megaDashUrl) {
            this.megaDashUrl = megaDashUrl;
            return this;
        }

        public ConfigDto build() {
            return new ConfigDto(this);
        }
    }
}
