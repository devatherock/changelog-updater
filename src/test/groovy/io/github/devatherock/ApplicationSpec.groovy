package io.github.devatherock

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

import groovy.json.JsonOutput

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
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
                    'w': 'working-directory',
                    'c': 'changelog-file',
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
                    'w': Paths.get(System.properties['user.dir'], 'src/test/resources/input')
                            .toAbsolutePath().toString(),
                    'c': 'changelog-one.md'
            ],
            requiredOptionsLong   : [:],
            additionalOptionsLong : [:],
    ]

    @Shared
    WireMockServer mockServer = new WireMockServer(8081)

    String githubBaseUrl = '/repos/Test/test-repo'
    Path inputFilePath = Paths.get(System.properties['user.dir'], 'src/test/resources/input/changelog-one.md')
    String initialContent

    void setupSpec() {
        formLongOptions(testContext.requiredOptionsShort, testContext.requiredOptionsLong)
        formLongOptions(testContext.additionalOptionsShort, testContext.additionalOptionsLong)

        WireMock.configureFor(8081)
        mockServer.start()
    }

    void cleanupSpec() {
        mockServer.stop()
    }

    void setup() {
        initialContent = inputFilePath.toFile().text
    }

    void cleanup() {
        mockServer.resetRequests()
        Files.write(inputFilePath, initialContent.bytes, StandardOpenOption.TRUNCATE_EXISTING)
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
        String getPullRequestFilesUrl = "${githubBaseUrl}/pulls/15/files"
        WireMock.givenThat(WireMock.get(getPullRequestFilesUrl)
                .willReturn(WireMock.okJson(JsonOutput.toJson([
                        [
                                'status'  : 'modified',
                                'filename': 'changelog-one.md'
                        ],
                        [
                                'status'  : 'added',
                                'filename': 'LICENSE',
                                'dummy_key': 'dummy_value',
                        ]
                ]))))

        when:
        int exitCode = PicocliRunner.execute(Application, args as String[])

        then:
        exitCode == 0

        and:
        WireMock.verify(1, createTestGetRequest(getPullRequestFilesUrl))
        WireMock.verify(0, WireMock.getRequestedFor(urlEqualTo("${githubBaseUrl}/releases")))

        where:
        optionPrefix | dryRunFlag | requiredOptions                  | additionalOptions
        '-'          | 'd'        | testContext.requiredOptionsShort | testContext.additionalOptionsShort
        '--'         | 'dry-run'  | testContext.requiredOptionsLong  | testContext.additionalOptionsLong
    }

    void 'test run - build event is a pull request, changelog not modified in the pull request'() {
        given:
        def options = new LinkedHashMap(requiredOptions)
        options.putAll(additionalOptions)
        options[dryRunFlag] = null
        def args = formArguments(options, optionPrefix)

        and:
        String getPullRequestFilesUrl = "${githubBaseUrl}/pulls/15/files"
        WireMock.givenThat(WireMock.get(getPullRequestFilesUrl)
                .willReturn(WireMock.okJson(JsonOutput.toJson([
                        [
                                'status'  : 'added',
                                'filename': 'LICENSE',
                        ]
                ]))))

        and:
        String getReleasesUrl = "${githubBaseUrl}/releases"
        WireMock.givenThat(WireMock.get(getReleasesUrl)
                .willReturn(WireMock.okJson(JsonOutput.toJson([
                        [
                                'tag_name'  : 'v1.0.0',
                                'draft': false,
                                'prerelease': false,
                                'published_at': '2022-08-27T12:22:44Z',
                                'dummy_key': 'dummy_value',
                        ]
                ]))))

        and:
        String getPullRequestUrl = "${githubBaseUrl}/pulls/15"
        WireMock.givenThat(WireMock.get(getPullRequestUrl)
                .willReturn(WireMock.okJson(JsonOutput.toJson([
                                'title': 'Entry five',
                                'dummy_key': 'dummy_value',
                ]))))

        when:
        int exitCode = PicocliRunner.execute(Application, args as String[])

        then:
        exitCode == 0

        and:
        WireMock.verify(1, createTestGetRequest(getPullRequestFilesUrl))
        WireMock.verify(1, createTestGetRequest(getReleasesUrl))
        WireMock.verify(1, createTestGetRequest(getPullRequestUrl))

        and:
        inputFilePath.toFile().text ==
                Paths.get(System.properties['user.dir'], 'src/test/resources/output/changelog-one-out.md')
                        .toFile().text

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

    private RequestPatternBuilder createTestGetRequest(String url) {
        return WireMock.getRequestedFor(urlEqualTo(url))
                .withHeader('Authorization', equalTo('dummyToken'))
                .withHeader('user-agent', equalTo('changelog-updater'))
    }
}