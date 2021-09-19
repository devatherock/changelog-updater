package io.github.devatherock.changelog.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestFile {
    private String status;

    private String filename;

    @JsonProperty("contents_url")
    private String contentsUrl;
}
