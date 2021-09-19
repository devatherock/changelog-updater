package io.github.devatherock.changelog.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import io.github.devatherock.changelog.entities.GithubRelease;
import io.github.devatherock.changelog.entities.PullRequest;
import io.github.devatherock.changelog.entities.PullRequestFile;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class GithubService {
    private final BlockingHttpClient githubClient;

    @Setter
    private String gitToken;

    @Setter
    private String gitRepoBaseUrl;

    public List<PullRequestFile> getPullRequestFiles(String pullRequestNumber) {
        HttpRequest<Object> filesListRequest = HttpRequest
                .create(HttpMethod.GET,
                        String.format("%s/pulls/%s/files", gitRepoBaseUrl, pullRequestNumber))
                .header("Authorization", gitToken)
                .header("user-agent", "changelog-updater");
        List<PullRequestFile> filesList = null;

        try {
            filesList = githubClient.retrieve(filesListRequest,
                    Argument.of(List.class, PullRequestFile.class));
        } catch (HttpClientResponseException exception) {
            LOGGER.error("Exception when reading files list of PR {}", pullRequestNumber, exception);
            ChangelogUpdaterHelper.exitWithError();
        }

        return filesList;
    }

    public String getPullRequestTitle(String pullRequestNumber) {
        HttpRequest<Object> filesListRequest = HttpRequest
                .create(HttpMethod.GET,
                        String.format("%s/pulls/%s", gitRepoBaseUrl, pullRequestNumber))
                .header("Authorization", gitToken)
                .header("user-agent", "changelog-updater");
        PullRequest pullRequest = null;

        try {
            pullRequest = githubClient.retrieve(filesListRequest, PullRequest.class);
        } catch (HttpClientResponseException exception) {
            LOGGER.error("Exception when reading title of PR {}", pullRequestNumber, exception);
            ChangelogUpdaterHelper.exitWithError();
        }

        return pullRequest.getTitle();
    }

    /**
     * Gets the latest release that starts with the specified prefix
     *
     * @param prefix
     * @param suffix
     * @return {@link GithubRelease}
     */
    public GithubRelease getLatestRelease(String prefix, String suffix) {
        HttpRequest<Object> listReleasesRequest = HttpRequest
                .create(HttpMethod.GET,
                        String.format("%s/releases", gitRepoBaseUrl))
                .header("Authorization", gitToken)
                .header("user-agent", "changelog-updater");
        List<GithubRelease> releasesList = null;

        try {
            releasesList = githubClient.retrieve(listReleasesRequest,
                    Argument.of(List.class, GithubRelease.class));
        } catch (HttpClientResponseException exception) {
            LOGGER.error("Exception when listing releases", exception);
            ChangelogUpdaterHelper.exitWithError();
        }

        Pattern releaseRegex = Pattern
                .compile("^" + prefix.replace(".", "\\.") + "[\\.0-9]*"
                        + suffix.replace(".", "\\.") + ".*$");
        LOGGER.debug("Release regex: {}", releaseRegex);

        GithubRelease latestRelease = null;
        Optional<GithubRelease> latestReleaseOptional = releasesList.stream()
                .filter(release -> !release.isDraft() && !release.isPrerelease()
                        && releaseRegex.matcher(release.getTagName()).matches())
                .sorted((first, second) -> {
                    if (first.getPublishedAt().getTime() > second.getPublishedAt().getTime()) {
                        return -1;
                    } else if (first.getPublishedAt().getTime() < second.getPublishedAt().getTime()) {
                        return 1;
                    } else {
                        return 0;
                    }
                })
                .findFirst();

        if (latestReleaseOptional.isPresent()) {
            latestRelease = latestReleaseOptional.get();
        }

        return latestRelease;
    }

}
