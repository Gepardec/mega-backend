package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gepardec.mega.application.constant.DateTimeConstants;
import com.gepardec.mega.application.jackson.serializer.DurationSerializer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationInfoDto {
    @JsonProperty
    private String version;

    @JsonProperty
    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_PATTERN)
    private LocalDateTime buildDate;

    @JsonProperty
    private Integer buildNumber;

    @JsonProperty
    private String commit;

    @JsonProperty
    private String branch;

    @JsonProperty
    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_PATTERN)
    private LocalDateTime startedAt;

    @JsonProperty
    @JsonSerialize(using = DurationSerializer.class)
    private Duration upTime;

    public ApplicationInfoDto(String version, LocalDateTime buildDate, Integer buildNumber, String commit, String branch, LocalDateTime startedAt, Duration upTime) {
        this.version = version;
        this.buildDate = buildDate;
        this.buildNumber = buildNumber;
        this.commit = commit;
        this.branch = branch;
        this.startedAt = startedAt;
        this.upTime = upTime;
    }

    public static ApplicationInfoDtoBuilder builder() {
        return ApplicationInfoDtoBuilder.anApplicationInfoDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationInfoDto that = (ApplicationInfoDto) o;
        return Objects.equals(version, that.version) && Objects.equals(buildDate, that.buildDate) && Objects.equals(buildNumber, that.buildNumber) && Objects.equals(commit, that.commit) && Objects.equals(branch, that.branch) && Objects.equals(startedAt, that.startedAt) && Objects.equals(upTime, that.upTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, buildDate, buildNumber, commit, branch, startedAt, upTime);
    }

    public static final class ApplicationInfoDtoBuilder {
        private String version;
        private LocalDateTime buildDate;
        private Integer buildNumber;
        private String commit;
        private String branch;
        private LocalDateTime startedAt;
        private Duration upTime;

        private ApplicationInfoDtoBuilder() {
        }

        public static ApplicationInfoDtoBuilder anApplicationInfoDto() {
            return new ApplicationInfoDtoBuilder();
        }

        public ApplicationInfoDtoBuilder version(String version) {
            this.version = version;
            return this;
        }

        public ApplicationInfoDtoBuilder buildDate(LocalDateTime buildDate) {
            this.buildDate = buildDate;
            return this;
        }

        public ApplicationInfoDtoBuilder buildNumber(Integer buildNumber) {
            this.buildNumber = buildNumber;
            return this;
        }

        public ApplicationInfoDtoBuilder commit(String commit) {
            this.commit = commit;
            return this;
        }

        public ApplicationInfoDtoBuilder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public ApplicationInfoDtoBuilder startedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public ApplicationInfoDtoBuilder upTime(Duration upTime) {
            this.upTime = upTime;
            return this;
        }

        @JsonProperty
        public void setVersion(String version) {
            this.version = version;
        }

        @JsonProperty
        public void setBuildDate(LocalDateTime buildDate) {
            this.buildDate = buildDate;
        }

        @JsonProperty
        public void setBuildNumber(Integer buildNumber) {
            this.buildNumber = buildNumber;
        }

        @JsonProperty
        public void setCommit(String commit) {
            this.commit = commit;
        }

        @JsonProperty
        public void setBranch(String branch) {
            this.branch = branch;
        }

        @JsonProperty
        public void setStartedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
        }

        @JsonProperty
        public void setUpTime(Duration upTime) {
            this.upTime = upTime;
        }

        public ApplicationInfoDto build() {
            return new ApplicationInfoDto(version, buildDate, buildNumber, commit, branch, startedAt, upTime);
        }
    }
}