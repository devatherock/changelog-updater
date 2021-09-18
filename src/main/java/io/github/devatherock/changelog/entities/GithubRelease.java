package io.github.devatherock.changelog.entities;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRelease {

    @JsonProperty("tag_name")
    private String tagName;

    private boolean draft;

    private boolean prerelease;

    @JsonProperty("published_at")
    private Date publishedAt;
}
