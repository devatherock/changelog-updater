package io.github.devatherock.changelog.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Class representing a file modified in a pull request
 * 
 * @author devaprasadh
 *
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestFile {
    private String status;

    private String filename;
}
