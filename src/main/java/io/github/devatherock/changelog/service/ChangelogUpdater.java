package io.github.devatherock.changelog.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import javax.inject.Singleton;

import io.github.devatherock.changelog.entities.ChangelogUpdateRequest;
import io.github.devatherock.changelog.entities.GithubRelease;
import io.github.devatherock.changelog.entities.PullRequestFile;
import io.github.devatherock.changelog.util.ChangelogUtility;
import io.github.devatherock.changelog.util.ProcessUtil;

import io.micronaut.core.annotation.Blocking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Class that handles changelog updates
 * 
 * @author devaprasadh
 *
 */
@Slf4j
@Blocking
@Singleton
@RequiredArgsConstructor
public class ChangelogUpdater {
    private final GithubService githubService;

    /**
     * Updates the changelog if it hasn't been already
     * 
     * @param request
     */
    public void updateChangelog(ChangelogUpdateRequest request) {
        if (isChangelogUpdated(request)) {
            LOGGER.info("{} is already updated in PR {}", request.getChangelogFile(), request.getPullRequestNumber());
        } else {
            LOGGER.warn("{} isn't updated in PR {}. Updating it now", request.getChangelogFile(),
                    request.getPullRequestNumber());

            writeChangelogEntry(request);

            if (request.isDryRun()) {
                LOGGER.info("Skipping git update as dry run flag is set");
            } else {
                if (request.isCi()) {
                    ProcessUtil.executeCommand("git config user.name " + request.getUsername(), true);
                    ProcessUtil.executeCommand("git config user.email " + request.getEmail(), true);
                }

                ProcessUtil.executeCommand("cd " + request.getWorkingDirectory() + System.lineSeparator()
                        + "git add " + request.getChangelogFile() + System.lineSeparator()
                        + "git commit -m 'chore(changelog-updater): Added changelog entry'" + System.lineSeparator()
                        + "git push origin " + request.getPullRequestBranch(), true);
            }
        }
    }

    /**
     * Checks if the changelog has been updated in the pull request
     * 
     * @param request
     * @return a flag
     */
    private boolean isChangelogUpdated(ChangelogUpdateRequest request) {
        boolean updated = false;

        Optional<PullRequestFile> changelogFile = githubService.getPullRequestFiles(request.getPullRequestNumber())
                .stream()
                .filter(prFile -> prFile.getFilename().equals(request.getChangelogFile()))
                .findFirst();

        if (changelogFile.isPresent()) {
            if ("removed".equals(changelogFile.get().getStatus())) {
                LOGGER.error("{} shouldn't be deleted", request.getChangelogFile());
                ChangelogUpdaterHelper.exitWithError();
            } else {
                updated = true;
            }
        }

        return updated;
    }

    /**
     * Writes an entry into the changelog
     * 
     * @param request
     */
    private void writeChangelogEntry(ChangelogUpdateRequest request) {
        GithubRelease latestRelease = githubService.getLatestRelease(request.getReleasePrefix(),
                request.getReleaseSuffix());
        String entry = githubService.getPullRequestTitle(request.getPullRequestNumber());
        String absoluteChangelogFilePath = Paths.get(request.getWorkingDirectory(), request.getChangelogFile())
                .toString();

        try {
            if (null != latestRelease) {
                String latestVersionInChangelog = ChangelogUtility.getLatestVersion(absoluteChangelogFilePath);

                if (null != latestVersionInChangelog) {
                    String latestVersionInGit = latestRelease.getTagName()
                            .substring(request.getReleasePrefix().length());

                    if (latestVersionInChangelog.equals(latestVersionInGit)) {
                        writeChangelogEntry(absoluteChangelogFilePath, entry, request.getNextVersion());
                    } else if (ChangelogUpdaterHelper.compareVersions(
                            latestVersionInChangelog, latestVersionInGit,
                            ChangelogUpdaterHelper.VERSION_PART_MAJOR) == -1) {
                        ChangelogUtility.writeToChangelog(absoluteChangelogFilePath, entry,
                                ChangelogUtility.ENTRY_TYPE_CHANGED,
                                latestVersionInGit, latestRelease.getPublishedAt(), request.getNextVersion());
                    } else {
                        writeChangelogEntry(absoluteChangelogFilePath, entry, request.getNextVersion());
                    }
                } else {
                    writeChangelogEntry(absoluteChangelogFilePath, entry, request.getNextVersion());
                }
            } else {
                writeChangelogEntry(absoluteChangelogFilePath, entry, request.getNextVersion());
            }
        } catch (IOException exception) {
            LOGGER.error("Exception when writing to {}", request.getChangelogFile(), exception);
            ChangelogUpdaterHelper.exitWithError();
        }
    }

    /**
     * Writes an entry into the changelog
     * 
     * @param changelogFilePath
     * @param entry
     * @param nextVersion
     * @throws IOException
     */
    private void writeChangelogEntry(String changelogFilePath, String entry, String nextVersion) throws IOException {
        ChangelogUtility.writeToChangelog(changelogFilePath, entry, ChangelogUtility.ENTRY_TYPE_CHANGED,
                nextVersion);
    }

}
