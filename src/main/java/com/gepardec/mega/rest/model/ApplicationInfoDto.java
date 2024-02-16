package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gepardec.mega.application.constant.DateTimeConstants;
import com.gepardec.mega.application.jackson.serializer.DurationSerializer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationInfoDto {
    private final String version;

    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_PATTERN)
    private final LocalDateTime buildDate;

    private final Integer buildNumber;

    private final String commit;

    private final String branch;

    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_PATTERN)
    private final LocalDateTime startedAt;

    @JsonSerialize(using = DurationSerializer.class)
    private final Duration upTime;

    private ApplicationInfoDto(Builder builder) {
        this.version = builder.version;
        this.buildDate = builder.buildDate;
        this.buildNumber = builder.buildNumber;
        this.commit = builder.commit;
        this.branch = builder.branch;
        this.startedAt = builder.startedAt;
        this.upTime = builder.upTime;
    }

    public String getVersion() {
        return version;
    }

    public LocalDateTime getBuildDate() {
        return buildDate;
    }

    public Integer getBuildNumber() {
        return buildNumber;
    }

    public String getCommit() {
        return commit;
    }

    public String getBranch() {
        return branch;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public Duration getUpTime() {
        return upTime;
    }

    public static Builder builder() {
        return Builder.anApplicationInfoDto();
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

    public static final class Builder {
        private String version;
        private LocalDateTime buildDate;
        private Integer buildNumber;
        private String commit;
        private String branch;
        private LocalDateTime startedAt;
        private Duration upTime;

        private Builder() {
        }

        public static Builder anApplicationInfoDto() {
            return new Builder();
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder buildDate(LocalDateTime buildDate) {
            this.buildDate = buildDate;
            return this;
        }

        public Builder buildNumber(Integer buildNumber) {
            this.buildNumber = buildNumber;
            return this;
        }

        public Builder commit(String commit) {
            this.commit = commit;
            return this;
        }

        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public Builder startedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder upTime(Duration upTime) {
            this.upTime = upTime;
            return this;
        }

        public ApplicationInfoDto build() {
            return new ApplicationInfoDto(this);
        }
    }
}
