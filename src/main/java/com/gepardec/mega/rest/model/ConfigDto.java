package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigDto {
    @JsonProperty
    private String excelUrl;

    @JsonProperty
    private String zepOrigin;

    @JsonProperty
    private String clientId;

    @JsonProperty
    private String issuer;

    @JsonProperty
    private String scope;

    @JsonProperty
    private String version;

    @JsonProperty
    private List<String> omMailAddresses;

    @JsonProperty
    private String subjectPrefix;

    @JsonProperty
    private String megaDashUrl;

    public ConfigDto() {
    }

    public ConfigDto(String excelUrl, String zepOrigin, String clientId, String issuer, String scope, String version, List<String> omMailAddresses, String subjectPrefix, String megaDashUrl) {
        this.excelUrl = excelUrl;
        this.zepOrigin = zepOrigin;
        this.clientId = clientId;
        this.issuer = issuer;
        this.scope = scope;
        this.version = version;
        this.omMailAddresses = omMailAddresses;
        this.subjectPrefix = subjectPrefix;
        this.megaDashUrl = megaDashUrl;
    }

    public static ConfigDtoBuilder builder() {
        return ConfigDtoBuilder.aConfigDto();
    }

    public String getExcelUrl() {
        return excelUrl;
    }

    public void setExcelUrl(String excelUrl) {
        this.excelUrl = excelUrl;
    }

    public String getZepOrigin() {
        return zepOrigin;
    }

    public void setZepOrigin(String zepOrigin) {
        this.zepOrigin = zepOrigin;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getOmMailAddresses() {
        return omMailAddresses;
    }

    public void setOmMailAddresses(List<String> omMailAddresses) {
        this.omMailAddresses = omMailAddresses;
    }

    public String getSubjectPrefix() {
        return subjectPrefix;
    }

    public void setSubjectPrefix(String subjectPrefix) {
        this.subjectPrefix = subjectPrefix;
    }

    public String getMegaDashUrl() {
        return megaDashUrl;
    }

    public void setMegaDashUrl(String megaDashUrl) {
        this.megaDashUrl = megaDashUrl;
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

    public static final class ConfigDtoBuilder {
        private String excelUrl;
        private String zepOrigin;
        private String clientId;
        private String issuer;
        private String scope;
        private String version;
        private List<String> omMailAddresses;
        private String subjectPrefix;
        private String megaDashUrl;

        private ConfigDtoBuilder() {
        }

        public static ConfigDtoBuilder aConfigDto() {
            return new ConfigDtoBuilder();
        }

        public ConfigDtoBuilder excelUrl(String excelUrl) {
            this.excelUrl = excelUrl;
            return this;
        }

        public ConfigDtoBuilder zepOrigin(String zepOrigin) {
            this.zepOrigin = zepOrigin;
            return this;
        }

        public ConfigDtoBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public ConfigDtoBuilder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public ConfigDtoBuilder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public ConfigDtoBuilder version(String version) {
            this.version = version;
            return this;
        }

        public ConfigDtoBuilder omMailAddresses(List<String> omMailAddresses) {
            this.omMailAddresses = omMailAddresses;
            return this;
        }

        public ConfigDtoBuilder subjectPrefix(String subjectPrefix) {
            this.subjectPrefix = subjectPrefix;
            return this;
        }

        public ConfigDtoBuilder megaDashUrl(String megaDashUrl) {
            this.megaDashUrl = megaDashUrl;
            return this;
        }

        public ConfigDto build() {
            ConfigDto configDto = new ConfigDto();
            configDto.setExcelUrl(excelUrl);
            configDto.setZepOrigin(zepOrigin);
            configDto.setClientId(clientId);
            configDto.setIssuer(issuer);
            configDto.setScope(scope);
            configDto.setVersion(version);
            configDto.setOmMailAddresses(omMailAddresses);
            configDto.setSubjectPrefix(subjectPrefix);
            configDto.setMegaDashUrl(megaDashUrl);
            return configDto;
        }
    }
}
