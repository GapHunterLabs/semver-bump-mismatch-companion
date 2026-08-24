package dev.gaphunter.semverbumpmismatchcompanion.parse

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogParserTest {

    @Test
    fun `parses release headers with a date`() {
        val text = """
            ## [Unreleased]

            ## [1.2.0] - 2026-08-20
            ### Added
            - Something new.

            ## [1.1.0] - 2026-08-01
            ### Added
            - Older thing.
        """.trimIndent()

        val entries = ChangelogParser.parse(text)
        assertEquals(3, entries.size)
        assertEquals("Unreleased", entries[0].version)
        assertEquals("1.2.0", entries[1].version)
        assertEquals("1.1.0", entries[2].version)
    }

    @Test
    fun `parses release headers with no date`() {
        val entries = ChangelogParser.parse("## [2.0.0]\nsome text\n")
        assertEquals(1, entries.size)
        assertEquals("2.0.0", entries[0].version)
    }

    @Test
    fun `body text is everything between one header and the next`() {
        val text = "## [1.1.0]\nAdded X.\nAdded Y.\n## [1.0.0]\nInitial.\n"
        val entries = ChangelogParser.parse(text)
        assertTrue(entries[0].bodyText.contains("Added X."))
        assertTrue(entries[0].bodyText.contains("Added Y."))
        assertTrue(!entries[0].bodyText.contains("Initial."))
    }

    @Test
    fun `the last entry's body runs to the end of the file`() {
        val text = "## [1.0.0]\nOnly release.\n"
        val entries = ChangelogParser.parse(text)
        assertTrue(entries[0].bodyText.contains("Only release."))
    }

    @Test
    fun `a file with no headers at all produces an empty list, not a crash`() {
        assertTrue(ChangelogParser.parse("Just some prose, no headers here.").isEmpty())
    }
}
