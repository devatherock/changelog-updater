package io.github.devatherock.changelog.util

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test class for {@link ProcessUtil}
 */
class ProcessUtilSpec extends Specification {

    @Unroll
    void 'test execute command - #command, #shell'() {
        expect:
        ProcessUtil.executeCommand(command, shell) == exitCode

        where:
        shell | exitCode | command
        true  | 0        | 'java -version'
        true  | 0        | ['java', '-version'] as String[]
        false | 0        | 'java -version'
        true  | 0        | 'echo $PATH'
        false | 1        | 'java -bola'
    }
}
