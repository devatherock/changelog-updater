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

    @Unroll
    void 'test write entry under new subheader - eof reached - #inputFile'() {
        given:
        Path path = Paths.get(System.properties['user.dir'], "src/test/resources/${inputFile}")
        String initialContent = path.toFile().text

        when:
        ChangelogUtility.writeToChangelog(path.toString(), 'Entry two', entryType, null)
        String finalContent = path.toFile().text

        then:
        finalContent ==
                Paths.get(System.properties['user.dir'], "src/test/resources/${outputFile}").toFile().text

        cleanup:
        Files.write(path, initialContent.bytes, StandardOpenOption.TRUNCATE_EXISTING)

        where:
        entryType | inputFile                 | outputFile
        'Changed' | 'input/changelog-four.md' | 'output/changelog-four-out.md'
        'Added'   | 'input/changelog-five.md' | 'output/changelog-five-out.md'
    }

    void 'test write entry to empty file'() {
        given:
        Path path = Paths.get(System.properties['user.dir'], 'src/test/resources/input/empty-file.md')
        String initialContent = path.toFile().text

        when:
        ChangelogUtility.writeToChangelog(path.toString(), 'Entry one')
        String finalContent = path.toFile().text

        then:
        finalContent == initialContent
    }

    @Unroll
    void 'test write entry under existing subheader - #nextVersion'() {
        given:
        Path path = Paths.get(System.properties['user.dir'], 'src/test/resources/input/changelog-one.md')
        String initialContent = path.toFile().text
        String entry = 'Entry five'

        when:
        ChangelogUtility.writeToChangelog(path.toString(), 'Entry five', 'Added', nextVersion)
        String finalContent = path.toFile().text

        then:
        finalContent ==
                Paths.get(System.properties['user.dir'], 'src/test/resources/output/changelog-one-out2.md').toFile().text

        cleanup:
        Files.write(path, initialContent.bytes, StandardOpenOption.TRUNCATE_EXISTING)
        
        where:
        nextVersion << [
            null,
            'Unreleased'
        ]
    }

    @Unroll
    void 'test write entry under new version - #filePath'() {
        given:
        Path path = Paths.get(System.properties['user.dir'], "src/test/resources/$filePath")
        String initialContent = path.toFile().text

        when:
        ChangelogUtility.writeToChangelog(path.toString(), 'Entry five', 'Removed', '1.0.1',
                new Date(1631478592000), 'Unreleased')
        String finalContent = path.toFile().text

        then:
        finalContent ==
                Paths.get(System.properties['user.dir'], 'src/test/resources/output/changelog-one-out3.md').toFile().text

        cleanup:
        Files.write(path, initialContent.bytes, StandardOpenOption.TRUNCATE_EXISTING)
        
        where:
        filePath << [
            'input/changelog-one.md',
            'input/changelog-three.md'
        ]
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
