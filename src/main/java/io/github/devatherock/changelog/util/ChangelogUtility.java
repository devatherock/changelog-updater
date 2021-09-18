package io.github.devatherock.changelog.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to read and write to a changelog file
 * 
 * @author devatherock
 *
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangelogUtility {
    public static final String ENTRY_TYPE_CHANGED = "Changed";

    private static final String DEF_CHANGELOG_SUBHEADER_PREFIX = "###";
    private static final String DEF_CHANGELOG_VERSION_LINE_PREFIX = "##";
    private static final String DEF_CHANGELOG_LINE_PREFIX = "-";
    private static final String DEF_NEXT_VERSION = "Unreleased";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    /**
     * Map that defines entry type order
     */
    private static final Map<String, Integer> ORDER_ENTRY_TYPE = new HashMap<>();

    static {
        ORDER_ENTRY_TYPE.put("Added", 0);
        ORDER_ENTRY_TYPE.put(ENTRY_TYPE_CHANGED, 1);
        ORDER_ENTRY_TYPE.put("Removed", 2);
    }

    public static void writeToChangelog(String changelogPathText, String entry)
            throws IOException {
        writeToChangelog(changelogPathText, entry, ENTRY_TYPE_CHANGED, null);
    }

    public static void writeToChangelog(String changelogPathText, String entry, String entryType,
                                        String nextVersion)
            throws IOException {
        writeToChangelog(changelogPathText, entry, entryType, null, null, nextVersion);
    }

    public static void writeToChangelog(String changelogPathText, String entry, String entryType,
                                        String currentVersion, Date releaseDate, String nextVersion)
            throws IOException {
        writeToChangelog(changelogPathText, entry, entryType, currentVersion, releaseDate,
                DEF_CHANGELOG_VERSION_LINE_PREFIX,
                DEF_CHANGELOG_LINE_PREFIX, nextVersion);
    }

    public static void writeToChangelog(String changelogPathText, String entry, String entryType,
                                        String currentVersion, Date releaseDate, String changelogVersionLinePrefix,
                                        String changelogLinePrefix,
                                        String nextVersion)
            throws IOException {
        Path changelogPath = getChangelogPath(changelogPathText);

        if (null != changelogPath) {
            List<String> fileContent = new ArrayList<>();
            Pattern changelogVersionLinePattern = Pattern.compile(changelogVersionLinePrefix +
                    "\\s[\\[]{1}(.*)\\].*");
            Pattern requiredSubHeaderPattern = Pattern.compile(DEF_CHANGELOG_SUBHEADER_PREFIX +
                    "\\s" + entryType);
            Pattern subHeaderPattern = Pattern.compile(DEF_CHANGELOG_SUBHEADER_PREFIX +
                    "\\s(Added|Changed|Removed)[\\s]*");

            Matcher changelogVersionLineMatcher = changelogVersionLinePattern.matcher("dummy");
            Matcher requiredSubHeaderMatcher = requiredSubHeaderPattern.matcher("dummy");
            Matcher subHeaderMatcher = subHeaderPattern.matcher("dummy");

            String currentVersionInChangelog = null;
            boolean entryWritten = false;
            boolean subHeaderMatchFound = false;
            int currentVersionIndex = 0;
            int index = -1;

            try (BufferedReader reader = new BufferedReader(new FileReader(changelogPath.toFile()))) {
                String currentLine = reader.readLine();

                while (null != currentLine) {
                    fileContent.add(currentLine);
                    index++;

                    if (null == currentVersionInChangelog) {
                        changelogVersionLineMatcher.reset(currentLine);

                        if (changelogVersionLineMatcher.matches()) {
                            currentVersionIndex = index;
                            currentVersionInChangelog = changelogVersionLineMatcher.group(1);

                            // Rewrite ## [Unreleased] to ## [x.x.x] - yyyy-MM-dd format
                            if (null != currentVersion && DEF_NEXT_VERSION.equals(currentVersionInChangelog)) {
                                replaceLastLine(fileContent, String.format("%s [%s] - %s", changelogVersionLinePrefix,
                                        currentVersion, DATE_FORMAT.format(releaseDate.toInstant())));
                                currentVersionInChangelog = currentVersion;
                            }

                            if (null != nextVersion && !nextVersion.equals(currentVersionInChangelog)) {
                                insertBeforeLast(fileContent, String.format("%s [%s]", changelogVersionLinePrefix,
                                        nextVersion));
                                insertBeforeLast(fileContent,
                                        String.format("%s %s", DEF_CHANGELOG_SUBHEADER_PREFIX, entryType));
                                insertBeforeLast(fileContent, String.format("%s %s", changelogLinePrefix, entry));
                                insertBeforeLast(fileContent, "");
                                entryWritten = true;
                            }
                        }
                    } else if (!entryWritten) {
                        changelogVersionLineMatcher.reset(currentLine);
                        requiredSubHeaderMatcher.reset(currentLine);

                        // Subheader not found
                        if (changelogVersionLineMatcher.matches()) {
                            insertEntryIntoLatestRelease(entryType, entry, changelogLinePrefix, fileContent,
                                    subHeaderMatcher, currentVersionIndex);
                            entryWritten = true;
                        }
                        // Matching subheader found
                        else if (requiredSubHeaderMatcher.matches()) {
                            subHeaderMatchFound = true;
                        }
                        // Last line reached under matching subheader
                        else if (subHeaderMatchFound && !currentLine.startsWith(changelogLinePrefix)) {
                            insertBeforeLast(fileContent, String.format("%s %s", changelogLinePrefix, entry));
                            entryWritten = true;
                        }
                    }

                    currentLine = reader.readLine();
                }

                // End of file reached before finding matching subheader
                if (!entryWritten) {
                    insertEntryIntoLatestRelease(entryType, entry, changelogLinePrefix, fileContent, subHeaderMatcher,
                            currentVersionIndex);
                }
            }

            // Write to the file
            Files.write(changelogPath,
                    String.join(System.lineSeparator(), fileContent).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private static void insertEntryIntoLatestRelease(String entryType, String entry, String changelogLinePrefix,
                                                     List<String> fileContent,
                                                     Matcher subHeaderMatcher, int currentVersionIndex) {
        int startIndex = 0;
        Stack<String> entryStack = new Stack<>();
        String currentLine = null;

        for (int reverseIndex = fileContent
                .size() - 1; reverseIndex > currentVersionIndex; reverseIndex--) {
            currentLine = fileContent.get(reverseIndex);
            subHeaderMatcher.reset(currentLine);
            entryStack.push(currentLine);

            if (subHeaderMatcher.matches()) {
                // New subheader has to be before this subheader
                if (compareEntryTypes(entryType, subHeaderMatcher.group(1)) < 0) {
                    startIndex = reverseIndex;
                    break;
                }
                // New subheader has to be after this subheader
                else {
                    startIndex = reverseIndex + 1;
                    entryStack.pop();

                    while (entryStack.pop().startsWith(changelogLinePrefix)) {
                        startIndex++;
                    }
                    startIndex++; // To start after existing empty line
                }
            }
        }

        if (startIndex != 0) {
            insertAt(fileContent, startIndex++,
                    String.format("%s %s", DEF_CHANGELOG_SUBHEADER_PREFIX, entryType));
            insertAt(fileContent, startIndex++, String.format("%s %s", changelogLinePrefix, entry));
            insertAt(fileContent, startIndex++, "");
        }
    }

    public static String getLatestVersion(String changelogPathText) throws IOException {
        return getLatestVersion(changelogPathText, DEF_CHANGELOG_VERSION_LINE_PREFIX);
    }

    public static String getLatestVersion(String changelogPathText, String changelogVersionLinePrefix)
            throws IOException {
        String latestVersion = null;
        Path changelogPath = getChangelogPath(changelogPathText);

        if (null != changelogPath) {
            Pattern changelogVersionLinePattern = Pattern.compile(changelogVersionLinePrefix +
                    "\\s[\\[]{1}(.*)\\]\\s-\\s[0-9]{4}-[0-9]{2}-[0-9]{2}");
            Matcher matcher = changelogVersionLinePattern.matcher("dummy");

            try (BufferedReader reader = new BufferedReader(new FileReader(changelogPath.toFile()))) {
                String currentLine = reader.readLine();

                while (null != currentLine) {
                    matcher.reset(currentLine);

                    // Exit the loop if a version line has been reached
                    if (matcher.matches()) {
                        latestVersion = matcher.group(1);
                        break;
                    }
                    currentLine = reader.readLine();
                }
            }
        }

        return latestVersion;
    }

    private static Path getChangelogPath(String changelogPathText) {
        Path changelogPath = Paths.get(changelogPathText);

        if (Files.exists(changelogPath)) {
            LOGGER.debug("changelog exists");
        } else {
            changelogPath = null;
            LOGGER.warn("File {} does not exist", changelogPathText);
        }

        return changelogPath;
    }

    private static void replaceLastLine(List<String> fileContent, String newLine) {
        fileContent.remove(fileContent.size() - 1);
        fileContent.add(newLine);
    }

    private static void insertBeforeLast(List<String> fileContent, String newLine) {
        insertAt(fileContent, fileContent.size() - 1, newLine);
    }

    private static void insertAt(List<String> fileContent, int index, String newLine) {
        fileContent.add(index, newLine);
    }

    private static int compareEntryTypes(String firstType, String secondType) {
        return ORDER_ENTRY_TYPE.getOrDefault(firstType, 3) - ORDER_ENTRY_TYPE.getOrDefault(secondType, 3);
    }
}
