package io.github.devatherock.changelog.util

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test class for {@link ChangelogUtility}
 */
class ChangelogUtilitySpec extends Specification {

    void 'test write entry'() {
        given:
        Path path = Paths.get(System.properties['user.dir'], 'src/test/resources/input/changelog-one.md')
        String initialContent = path.toFile().text

        when:
        ChangelogUtility.writeToChangelog(path.toString(), 'Entry five')
        String finalContent = path.toFile().text

        then:
        finalContent ==
                Paths.get(System.properties['user.dir'], 'src/test/resources/output/changelog-one-out.md').toFile().text

        cleanup:
        Files.write(path, initialContent.bytes, StandardOpenOption.TRUNCATE_EXISTING)
    }

    void 'test write entry under existing subheader'() {
        given:
        Path path = Paths.get(System.properties['user.dir'], 'src/test/resources/input/changelog-one.md')
        String initialContent = path.toFile().text
        String entry = 'Entry five'

        when:
        ChangelogUtility.writeToChangelog(path.toString(), 'Entry five', 'Added', null)
        String finalContent = path.toFile().text

        then:
        finalContent ==
                Paths.get(System.properties['user.dir'], 'src/test/resources/output/changelog-one-out2.md').toFile().text

        cleanup:
        Files.write(path, initialContent.bytes, StandardOpenOption.TRUNCATE_EXISTING)
    }

    void 'test write entry under new version'() {
        given:
        Path path = Paths.get(System.properties['user.dir'], 'src/test/resources/input/changelog-one.md')
        String initialContent = path.toFile().text

        when:
        ChangelogUtility.writeToChangelog(path.toString(), 'Entry five', 'Removed','1.0.1',
                new Date(1631478592000), 'Unreleased')
        String finalContent = path.toFile().text

        then:
        finalContent ==
                Paths.get(System.properties['user.dir'], 'src/test/resources/output/changelog-one-out3.md').toFile().text

        cleanup:
        Files.write(path, initialContent.bytes, StandardOpenOption.TRUNCATE_EXISTING)
    }

    void 'test write changelog - file does not exist'() {
        when:
        ChangelogUtility.writeToChangelog('dummy.md', 'dummy')

        then:
        noExceptionThrown()
    }

    @Unroll
    void 'test get latest version - #filePath'() {
        given:
        Path path = Paths.get(System.properties['user.dir'], "src/test/resources/${filePath}")

        expect:
        ChangelogUtility.getLatestVersion(path.toString()) == expectedVersion

        where:
        expectedVersion | filePath
        '1.0.0'         | 'input/changelog-one.md'
        '1.0.1'         | 'input/changelog-three.md'
        '1.0.0'         | 'input/changelog-two.md'
        '1.0.0'         | 'output/changelog-one-out.md'
        '1.0.1'         | 'output/changelog-one-out3.md'
        '1.0.0'         | 'output/changelog-one-out2.md'
        null            | 'non-existent.md'
        null            | 'input/empty-file.md'
    }
}
