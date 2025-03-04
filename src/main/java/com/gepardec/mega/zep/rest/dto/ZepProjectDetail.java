package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

//FIXME: Must be a class until jackson supports records with JsonUnwrapped (fixed in v.2.19.0 or 3.0.0)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepProjectDetail {

    @JsonUnwrapped
    private ZepProject project;

    @JsonProperty("categories")
    private List<ZepCategory> categories;

    public ZepProject getProject() {
        return project;
    }

    public void setProject(ZepProject project) {
        this.project = project;
    }

    public List<ZepCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<ZepCategory> categories) {
        this.categories = categories;
    }
}
