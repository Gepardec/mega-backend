package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record ZepEmployee (
        String username,
        String firstname,
        String lastname,
        ZepSalutation salutation,
        String title,
        String email,
        LocalDate releaseDate,
        String priceGroup,
        ZepLanguage language
) {

        @JsonCreator
        public ZepEmployee(Builder builder) {
                this(
                        builder.username,
                        builder.firstname,
                        builder.lastname,
                        builder.salutation,
                        builder.title,
                        builder.email,
                        builder.releaseDate,
                        builder.priceGroup,
                        builder.language
                );
        }

        public static Builder builder() {
                return Builder.aZepEmployee();
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Builder {
                @JsonProperty
                private String username;
                @JsonProperty
                private String firstname;
                @JsonProperty
                private String lastname;
                @JsonProperty
                private ZepSalutation salutation;
                @JsonProperty
                private String title;
                @JsonProperty
                private String email;

                @JsonProperty("release_date")
                private LocalDate releaseDate;
                @JsonProperty("price_group")
                private String priceGroup;
                @JsonProperty
                private ZepLanguage language;


                public Builder username(String username) {
                        this.username = username;
                        return this;
                }

                public Builder firstname(String firstname) {
                        this.firstname = firstname;
                        return this;
                }

                public Builder lastname(String lastname) {
                        this.lastname = lastname;
                        return this;
                }

                public Builder salutation(ZepSalutation salutation) {
                        this.salutation = salutation;
                        return this;
                }

                public Builder title(String title) {
                        this.title = title;
                        return this;
                }

                public Builder email(String email) {
                        this.email = email;
                        return this;
                }

                public Builder releaseDate(LocalDate releaseDate) {
                        this.releaseDate = releaseDate;
                        return this;
                }


                public Builder priceGroup(String priceGroup) {
                        this.priceGroup = priceGroup;
                        return this;
                }

                public Builder language(ZepLanguage language) {
                        this.language = language;
                        return this;
                }

                public ZepEmployee build() {
                        return new ZepEmployee(this);
                }


                public static Builder aZepEmployee() {
                        return new Builder();
                }
        }
}
