package dev.gaphunter.semverbumpmismatchcompanion.detect

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
    fun `an unparseable version is skipped without crashing`() {
        val entries = listOf(entry("not-a-version", "BREAKING stuff."), entry("1.0.0", "Initial."))
        assertTrue(SemverBumpChecker.findMismatches(entries).isEmpty())
    }
}
