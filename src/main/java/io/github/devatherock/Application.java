package io.github.devatherock;

import javax.inject.Inject;

import io.github.devatherock.changelog.entities.ChangelogUpdateRequest;
import io.github.devatherock.changelog.service.ChangelogUpdater;
import io.github.devatherock.changelog.service.GithubService;

import io.micronaut.configuration.picocli.PicocliRunner;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * The main class, that parses the command line args
 * 
 * @author devaprasadh
 *
 */
@Slf4j
@Command(name = "changelog-updater", description = "Adds a changelog file entry", mixinStandardHelpOptions = true)
public class Application implements Runnable {

    @Option(names = { "-t", "--token" }, required = true, description = "Github access token")
    private String gitToken;

    @Option(names = { "-a", "--api" }, defaultValue = "https://api.github.com", description = "Github API URL")
    private String gitUrl;

    @Option(names = { "-o", "--org" }, required = true, description = "Github organization")
    private String gitOrg;

    @Option(names = { "-r", "--repo" }, required = true, description = "Github repository name")
    private String repoName;

    @Option(names = { "-e", "--event" }, required = true, description = "Github event that triggered the build")
    private String buildEvent;

    @Option(names = { "-un", "--username" }, required = true, description = "Username of last committer")
    private String username;

    @Option(names = { "-ue", "--email" }, required = true, description = "Email address of last committer")
    private String email;

    @Option(names = { "-p",
            "--pull-request" }, required = true, description = "Number of the pull request that triggered the build")
    private String pullRequestNumber;

    @Option(names = { "-pb",
            "--pull-request-branch" }, required = true, description = "Source branch for the pull request that triggered the build")
    private String pullRequestBranch;

    @Option(names = { "-d",
            "--dry-run" }, description = "Flag to run the plugin without committing to git. Defaults to false.")
    private boolean dryRun;

    @Option(names = {
            "--ci" }, description = "Indicates if the environment is a CI environment. Defaults to true.")
    private boolean ci;

    @Option(names = { "-c",
            "--changelog-file" }, defaultValue = "CHANGELOG.md", description = "The changelog file name/path")
    private String changelogFile;

    @Option(names = { "-nv",
            "--next-version" }, defaultValue = "Unreleased", description = "Version number for the next release, to add to changelog")
    private String nextVersion;

    @Option(names = { "-vp",
            "--version-prefix" }, defaultValue = "v", description = "The release version prefix to look for")
    private String releasePrefix;

    @Option(names = { "-vs",
            "--version-suffix" }, defaultValue = "", description = "The release version suffix to look for")
    private String releaseSuffix;

    @Option(names = { "-w",
            "--working-directory" }, defaultValue = "${sys:user.dir}", description = "The working directory containing the git repo contents")
    private String workingDirectory;

    @Inject
    private ChangelogUpdater changelogUpdater;

    @Inject
    private GithubService githubService;

    public static void main(String[] args) {
        PicocliRunner.run(Application.class, args);
    }

    @Override
    public void run() {
        if ("pull_request".equals(buildEvent)) {
            ChangelogUpdateRequest request = ChangelogUpdateRequest.builder()
                    .gitToken(gitToken)
                    .gitUrl(gitUrl)
                    .gitOrg(gitOrg)
                    .repoName(repoName)
                    .username(username)
                    .email(email)
                    .pullRequestNumber(pullRequestNumber)
                    .pullRequestBranch(pullRequestBranch)
                    .dryRun(dryRun)
                    .ci(ci)
                    .changelogFile(changelogFile)
                    .nextVersion(nextVersion)
                    .releasePrefix(releasePrefix)
                    .releaseSuffix(releaseSuffix)
                    .workingDirectory(workingDirectory)
                    .build();

            String gitRepoBaseUrl = String.format("%s/repos/%s/%s", request.getGitUrl(), request.getGitOrg(),
                    request.getRepoName());
            githubService.setGitRepoBaseUrl(gitRepoBaseUrl);
            githubService.setGitToken(gitToken);

            changelogUpdater.updateChangelog(request);
        } else {
            LOGGER.warn("Skipping changelog updater as build event is {}", buildEvent);
        }
    }
}
