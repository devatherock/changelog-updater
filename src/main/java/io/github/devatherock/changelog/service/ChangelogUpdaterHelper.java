package io.github.devatherock.changelog.service;

import java.util.regex.Pattern;

public class ChangelogUpdaterHelper {
    /**
     * Major version part of a semantic version
     */
    public static final String VERSION_PART_MAJOR = "major";
    /**
     * Minor version part of a semantic version
     */
    private static final String VERSION_PART_MINOR = "minor";
    /**
     * Patch version part of a semantic version
     */
    private static final String VERSION_PART_PATCH = "patch";
    /**
     * Pattern to match numbers
     */
    private static final Pattern PTRN_NUMBER = Pattern.compile("^[0-9]+$");

    /**
     * Compares two versions
     *
     * @param versionOne
     * @param versionTwo
     * @param versionPartType
     * @return {@literal -1} if {@code versionTwo} greater than {@code versionOne},
     *         {@literal 1} otherwise
     */
    static int compareVersions(String versionOne, String versionTwo, String versionPartType) {
        int result = 0;

        int versionPartEndOne = getVersionPartEndIndex(versionOne);
        String versionPartOneText = versionOne.substring(0,
                versionPartEndOne != -1 ? versionPartEndOne : versionOne.length());
        long versionPartOne = readVersionAsNumber(versionPartOneText);
        int versionPartEndTwo = getVersionPartEndIndex(versionTwo);
        String versionPartTwoText = versionTwo.substring(0,
                versionPartEndTwo != -1 ? versionPartEndTwo : versionTwo.length());
        long versionPartTwo = readVersionAsNumber(versionPartTwoText);

        if (versionPartOne > versionPartTwo) {
            result = 1;
        } else if (versionPartOne < versionPartTwo) {
            result = -1;
        } else {
            if ((versionPartOneText.length() + 1) >= versionOne.length()) {
                if ((versionPartTwoText.length() + 1) < versionTwo.length()) {
                    result = -1;
                }
            } else if ((versionPartTwoText.length() + 1) < versionTwo.length()) {
                if (!VERSION_PART_PATCH.equals(versionPartType)) {
                    result = compareVersions(versionOne.substring(versionPartEndOne + 1),
                            versionTwo.substring(versionPartEndTwo + 1),
                            VERSION_PART_MAJOR.equals(versionPartType) ? VERSION_PART_MINOR : VERSION_PART_PATCH);
                }
            } else {
                result = 1;
            }
        }

        return result;
    }

    static void exitWithError() {
        System.exit(1);
    }

    /**
     * Returns the index at which the first version part ends
     *
     * @param version
     * @return the index
     */
    private static int getVersionPartEndIndex(String version) {
        return version.indexOf('.') != -1 ? version.indexOf('.') : version.indexOf('-');
    }

    /**
     * Converts version string into a number
     *
     * @param version
     * @return the version as number
     */
    private static long readVersionAsNumber(String version) {
        return PTRN_NUMBER.matcher(version).matches() ? Long.parseLong(version) : 0;
    }
}
