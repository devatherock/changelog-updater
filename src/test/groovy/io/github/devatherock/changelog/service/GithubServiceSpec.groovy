package io.github.devatherock.changelog.service

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

import groovy.json.JsonOutput

import io.github.devatherock.changelog.entities.PullRequestFile

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

/**
 * Test class for {@link GithubService}
 */
class GithubServiceSpec extends Specification {
    @Subject
    GithubService service

    @Shared
    WireMockServer mockServer = new WireMockServer(8081)

    void setupSpec() {
        WireMock.configureFor(8081)
        mockServer.start()
    }

    void cleanupSpec() {
        mockServer.stop()
    }

    void cleanup() {
        mockServer.resetRequests()
    }

    String gitUrl = 'http://localhost:8081'
    BlockingHttpClient httpClient = HttpClient.create(new URL(gitUrl)).toBlocking()

    void setup() {
        service = new GithubService(httpClient)
        service.gitRepoBaseUrl = gitUrl
        service.gitToken = 'dummyToken'
    }

    void 'test get pull request files - success'() {
        given:
        String pullRequestNumber = '15'

        and:
        WireMock.givenThat(WireMock.get("/pulls/${pullRequestNumber}/files")
                .willReturn(WireMock.okJson(JsonOutput.toJson([
                    [
                        'status': 'modified',
                        'filename': 'CHANGELOG.md'
                    ],
                    [
                        'status': 'added',
                        'filename': 'LICENSE'
                    ]
                ]))))

        when:
        List<PullRequestFile> files = service.getPullRequestFiles(pullRequestNumber)

        then:
        WireMock.verify(1,
                WireMock.getRequestedFor(urlEqualTo("/pulls/${pullRequestNumber}/files"))
                .withHeader('Authorization', equalTo('dummyToken'))
                .withHeader('user-agent', equalTo('changelog-updater')))

        and:
        files.size() == 2
        files[0].status == 'modified'
        files[0].filename == 'CHANGELOG.md'
        files[1].status == 'added'
        files[1].filename == 'LICENSE'
    }
}
