package io.github.devatherock.changelog.service

import spock.lang.Specification

/**
 * Test class for {@link ChangelogUpdaterHelper}
 */
class ChangelogUpdaterHelperSpec extends Specification {

    void 'test compare versions'() {
        expect:
        ChangelogUpdaterHelper.compareVersions(versionOne, versionTwo, 'major') == expectedResult

        where:
        versionOne    | versionTwo   | expectedResult
        '2'           | '1.1'        | 1
        '1'           | '1.1'        | -1
        '1.1-alpha'   | '1.1-beta'   | 0
        '1.1.1-alpha' | '1.1.1-beta' | 0
        '2.5-alpine'  | '2.5'        | 1
        '2.5'         | '2.5-alpine' | -1
        '1.1.1'       | '1.1.2'      | -1
        '1.1.2'       | '1.1.1'      | 1
        '1.1.2'       | '1.1.2'      | 0
    }
}
