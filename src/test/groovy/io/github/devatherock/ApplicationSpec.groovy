package io.github.devatherock

import io.micronaut.configuration.picocli.PicocliRunner
import spock.lang.Shared
import spock.lang.Specification

/**
 * Test class for {@link Application} class
 */
class ApplicationSpec extends Specification {

    @Shared
    def allOptions = [
            't' : 'token',
            'o' : 'org',
            'r' : 'repo',
            'un': 'username',
            'ue': 'email',
            'p' : 'pull-request',
            'pb': 'pull-request-branch',
    ]

    @Shared
    def requiredOptionsShort = [
            't' : 'test-token',
            'o' : 'Test',
            'r' : 'test-repo',
            'un': 'test',
            'ue': 'test@test.com',
            'p' : '1',
            'pb': 'feature/test',
    ]

    @Shared
    def requiredOptionsLong = [:]

    void setupSpec() {
        allOptions.each { shortOption, longOption ->
            requiredOptionsLong[longOption] = requiredOptionsShort[shortOption]
        }
    }

    void 'test run - required options not specified'() {
        given:
        def options = [:]
        options.putAll(requiredOptionsShort)
        options['e'] = 'push'

        and:
        options.remove(missingOption)
        def args = formArguments(options)

        when:
        int exitCode = PicocliRunner.execute(Application, args as String[])

        then:
        exitCode != 0

        where:
        missingOption << ['t', 'o', 'r', 'un', 'ue', 'p', 'pb', 'e']
    }

    void 'test run - build event is not a pull request'() {
        given:
        def args = formArguments(options, optionPrefix)
        args.add(argument)
        args.add('push')

        when:
        int exitCode = PicocliRunner.execute(Application, args as String[])

        then:
        exitCode == 0

        where:
        argument  | optionPrefix | options
        '-e'      | '-'          | requiredOptionsShort
        '--event' | '--'         | requiredOptionsLong
    }

    private def formArguments(def options, String optionPrefix = '-') {
        def args = []

        options.each { key, value ->
            args.add("${optionPrefix}${key}")
            args.add(value)
        }

        return args
    }
}