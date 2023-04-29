package io.github.devatherock

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

import groovy.json.JsonOutput

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.micronaut.configuration.picocli.PicocliRunner
import spock.lang.Shared
import spock.lang.Specification

/**
 * Test class for {@link Application} class
 */
class ApplicationSpec extends Specification {

    @Shared
    def testContext = [
            allOptions            : [
                    't' : 'token',
                    'o' : 'org',
                    'r' : 'repo',
                    'un': 'username',
                    'ue': 'email',
                    'p' : 'pull-request',
                    'pb': 'pull-request-branch',
                    'e' : 'event',
                    'a' : 'api',
            ],
            requiredOptionsShort  : [
                    't' : 'dummyToken',
                    'o' : 'Test',
                    'r' : 'test-repo',
                    'un': 'devatherock',
                    'ue': 'devatherock@gmail.com',
                    'p' : '15',
                    'pb': 'feature/test',
                    'e' : 'pull_request',
            ],
            additionalOptionsShort: [
                    'a': 'http://localhost:8081',
            ],
            requiredOptionsLong   : [:],
            additionalOptionsLong : [:],
    ]

    @Shared
    WireMockServer mockServer = new WireMockServer(8081)

    String githubBaseUrl = '/repos/Test/test-repo'

    void setupSpec() {
        formLongOptions(testContext.requiredOptionsShort, testContext.requiredOptionsLong)
        formLongOptions(testContext.additionalOptionsShort, testContext.additionalOptionsLong)

        WireMock.configureFor(8081)
        mockServer.start()
    }

    void cleanupSpec() {
        mockServer.stop()
    }

    void cleanup() {
        mockServer.resetRequests()
    }

    void 'test run - required options not specified'() {
        given:
        def options = new LinkedHashMap(testContext.requiredOptionsShort)

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
        options[argument] = 'push'
        def args = formArguments(options, optionPrefix)

        when:
        int exitCode = PicocliRunner.execute(Application, args as String[])

        then:
        exitCode == 0

        where:
        argument | optionPrefix | options
        'e'      | '-'          | new LinkedHashMap(testContext.requiredOptionsShort)
        'event'  | '--'         | new LinkedHashMap(testContext.requiredOptionsLong)
    }

    void 'test run - build event is a pull request, changelog modified in the pull request'() {
        given:
        def options = new LinkedHashMap(requiredOptions)
        options.putAll(additionalOptions)
        options[dryRunFlag] = null
        def args = formArguments(options, optionPrefix)

        and:
        String getPullRequestsUrl = "${githubBaseUrl}/pulls/15/files"
        WireMock.givenThat(WireMock.get(getPullRequestsUrl)
                .willReturn(WireMock.okJson(JsonOutput.toJson([
                        [
                                'status'  : 'modified',
                                'filename': 'CHANGELOG.md'
                        ],
                        [
                                'status'  : 'added',
                                'filename': 'LICENSE'
                        ]
                ]))))

        when:
        int exitCode = PicocliRunner.execute(Application, args as String[])

        then:
        exitCode == 0

        and:
        WireMock.verify(1,
                WireMock.getRequestedFor(urlEqualTo(getPullRequestsUrl))
                        .withHeader('Authorization', equalTo('dummyToken'))
                        .withHeader('user-agent', equalTo('changelog-updater')))
        WireMock.verify(0, WireMock.getRequestedFor(urlEqualTo("${githubBaseUrl}/releases")))

        where:
        optionPrefix | dryRunFlag | requiredOptions                  | additionalOptions
        '-'          | 'd'        | testContext.requiredOptionsShort | testContext.additionalOptionsShort
        '--'         | 'dry-run'  | testContext.requiredOptionsLong  | testContext.additionalOptionsLong
    }

    private def formArguments(def options, String optionPrefix = '-') {
        def args = []

        options.each { key, value ->
            args.add("${optionPrefix}${key}")

            if (value) { // To handle boolean flags without explicit value
                args.add(value)
            }
        }

        return args
    }

    private void formLongOptions(Map shortOptions, Map longOptions) {
        shortOptions.each { shortOption, value ->
            longOptions[testContext.allOptions[shortOption]] = value
        }
    }
}