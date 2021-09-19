package io.github.devatherock.changelog.entities;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChangelogUpdateRequest {
    private String gitToken;
    private String gitUrl;
    private String gitOrg;
    private String username;
    private String email;
    private String repoName;
    private String pullRequestNumber;
    private String pullRequestBranch;
    private boolean dryRun;
    private boolean ci;
    private String changelogFile;
    private String nextVersion;
    private String releasePrefix;
    private String releaseSuffix;
    private String workingDirectory;
}
