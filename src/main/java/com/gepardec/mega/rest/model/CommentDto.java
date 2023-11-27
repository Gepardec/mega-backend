package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.SourceSystem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String message;

    @JsonProperty
    private String authorEmail;

    @JsonProperty
    private String authorName;

    @JsonProperty
    private String updateDate;

    @JsonProperty
    private EmployeeState state;

    @JsonProperty
    private SourceSystem sourceSystem;
}
