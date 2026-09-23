package dev.gaphunter.semverbumpmismatchcompanion.detect

import dev.gaphunter.semverbumpmismatchcompanion.model.BreakingSignal
import dev.gaphunter.semverbumpmismatchcompanion.parse.ChangelogParser
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * The demo CHANGELOG.md as a tester actually opens it: read from
 * `demo/` on disk, so the walkthrough in `demo/README.md` cannot drift
 * away from what the checker does.
 */
class DemoProjectTest {

    @Test
    fun `only 1-4-1 is flagged, 1-4-2's non-breaking wording is not`() {
        val text = File("demo/CHANGELOG.md").readText()
        val hits = SemverBumpChecker.findMismatches(ChangelogParser.parse(text))
        assertEquals(listOf("1.4.1"), hits.map { it.entry.version })
        assertEquals(BreakingSignal.BREAKING_WORD, hits.single().signal)
    }
}
