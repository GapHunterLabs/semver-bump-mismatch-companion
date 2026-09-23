package dev.gaphunter.semverbumpmismatchcompanion.detect

import dev.gaphunter.semverbumpmismatchcompanion.model.BreakingSignal
import dev.gaphunter.semverbumpmismatchcompanion.model.ChangelogEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SemverBumpCheckerTest {

    private fun entry(version: String, body: String) = ChangelogEntry(version, 0, 0, body)

    @Test
    fun `a breaking release with only a minor bump is flagged`() {
        val entries = listOf(entry("1.3.0", "BREAKING: removed the old API."), entry("1.2.0", "Added a feature."))
        val hits = SemverBumpChecker.findMismatches(entries)
        assertEquals(1, hits.size)
        assertEquals("1.3.0", hits[0].entry.version)
        assertEquals("1.2.0", hits[0].previousVersion)
        assertEquals(BreakingSignal.BREAKING_WORD, hits[0].signal)
    }

    @Test
    fun `a breaking release with only a patch bump is flagged`() {
        val entries = listOf(entry("1.2.4", "BREAKING CHANGE in the response shape."), entry("1.2.3", "Fixed a bug."))
        assertEquals(1, SemverBumpChecker.findMismatches(entries).size)
    }

    @Test
    fun `a breaking release with a real major bump is not flagged`() {
        val entries = listOf(entry("2.0.0", "BREAKING: removed the old API."), entry("1.2.0", "Added a feature."))
        assertTrue(SemverBumpChecker.findMismatches(entries).isEmpty())
    }

    @Test
    fun `a release with a Removed section and only a minor bump is flagged, even with no literal BREAKING word`() {
        val entries = listOf(
            entry("1.3.0", "### Removed\n- The deprecated `oldMethod()`.\n"),
            entry("1.2.0", "Added a feature."),
        )
        val hits = SemverBumpChecker.findMismatches(entries)
        assertEquals(1, hits.size)
        assertEquals("1.3.0", hits[0].entry.version)
        assertEquals(BreakingSignal.REMOVED_SECTION, hits[0].signal)
    }

    @Test
    fun `a Removed section with a real major bump is not flagged`() {
        val entries = listOf(
            entry("2.0.0", "### Removed\n- The deprecated `oldMethod()`.\n"),
            entry("1.2.0", "Added a feature."),
        )
        assertTrue(SemverBumpChecker.findMismatches(entries).isEmpty())
    }

    @Test
    fun `a Deprecated section (not Removed) is never treated as a breaking-change signal`() {
        val entries = listOf(
            entry("1.3.0", "### Deprecated\n- `oldMethod()` will be removed in a future release.\n"),
            entry("1.2.0", "Added a feature."),
        )
        assertTrue(SemverBumpChecker.findMismatches(entries).isEmpty())
    }

    @Test
    fun `a non-breaking release with only a minor bump is not flagged`() {
        val entries = listOf(entry("1.3.0", "Added a feature."), entry("1.2.0", "Added another feature."))
        assertTrue(SemverBumpChecker.findMismatches(entries).isEmpty())
    }

    @Test
    fun `a 0-x-y major version is never flagged, even with BREAKING and no major bump`() {
        val entries = listOf(entry("0.3.0", "BREAKING: totally reworked the API."), entry("0.2.0", "Initial shape."))
        assertTrue(SemverBumpChecker.findMismatches(entries).isEmpty())
    }

    @Test
    fun `only 2 or more real entries can be compared -- a single entry produces no hits`() {
        val entries = listOf(entry("1.0.0", "BREAKING: first release."))
        assertTrue(SemverBumpChecker.findMismatches(entries).isEmpty())
    }

    @Test
    fun `a release explicitly saying non-breaking with only a minor bump is not flagged`() {
        val entries = listOf(
            entry("1.3.0", "This is a non-breaking change to the internal cache."),
            entry("1.2.0", "Added a feature."),
        )
        assertTrue(SemverBumpChecker.findMismatches(entries).isEmpty())
    }

    @Test
    fun `a release saying no breaking changes with only a minor bump is not flagged`() {
        val entries = listOf(
            entry("1.3.0", "No breaking changes in this release, just internal cleanup."),
            entry("1.2.0", "Added a feature."),
        )
        assertTrue(SemverBumpChecker.findMismatches(entries).isEmpty())
    }

    @Test
    fun `a release saying not breaking or without breaking changes is not flagged`() {
        val notBreaking = listOf(entry("1.3.0", "This isn't breaking anything downstream."), entry("1.2.0", "x"))
        val withoutBreaking = listOf(entry("1.3.0", "Refactored internals without breaking the public API."), entry("1.2.0", "x"))
        assertTrue(SemverBumpChecker.findMismatches(notBreaking).isEmpty())
        assertTrue(SemverBumpChecker.findMismatches(withoutBreaking).isEmpty())
    }

    @Test
    fun `a real BREAKING mention elsewhere in the same body as a negated one is still flagged`() {
        val entries = listOf(
            entry("1.3.0", "No breaking changes to the public API. BREAKING: internal plugin ABI changed."),
            entry("1.2.0", "Added a feature."),
        )
        assertEquals(1, SemverBumpChecker.findMismatches(entries).size)
    }

    @Test
    fun `known gap -- a negation word separated from breaking by another word still flags`() {
        // "not a breaking change" isn't recognized as negated (only a
        // direct "not/no/non/without/isn't/aren't breaking" is) --
        // documented limitation, not a target for this cycle.
        val entries = listOf(entry("1.3.0", "This is not a breaking change."), entry("1.2.0", "x"))
        assertEquals(1, SemverBumpChecker.findMismatches(entries).size)
    }

    @Test
    fun `an unparseable version is skipped without crashing`() {
        val entries = listOf(entry("not-a-version", "BREAKING stuff."), entry("1.0.0", "Initial."))
        assertTrue(SemverBumpChecker.findMismatches(entries).isEmpty())
    }
}
