package io.github.devatherock.changelog.service

import org.junit.Rule
import org.junit.contrib.java.lang.system.ExpectedSystemExit
import org.junit.contrib.java.lang.system.internal.CheckExitCalled

import spock.lang.Specification

/**
 * Test class for {@link ChangelogUpdaterHelper}
 */
class ChangelogUpdaterHelperSpec extends Specification {

    @Rule
    ExpectedSystemExit exitRule = ExpectedSystemExit.none()

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

    void 'test exit with error'() {
        given:
        exitRule.expectSystemExitWithStatus(1)

        when:
        ChangelogUpdaterHelper.exitWithError()

        then:
        CheckExitCalled exit = thrown()
        exit.status == 1
    }
}
