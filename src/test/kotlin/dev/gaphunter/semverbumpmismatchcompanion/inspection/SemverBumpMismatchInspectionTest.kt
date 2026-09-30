package dev.gaphunter.semverbumpmismatchcompanion.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SemverBumpMismatchInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(SemverBumpMismatchInspection::class.java)
    }

    fun `test a breaking release with only a minor bump produces a warning`() {
        myFixture.configureByText(
            "CHANGELOG.md",
            "## [1.3.0]\nBREAKING: removed the old API.\n\n## [1.2.0]\nAdded a feature.\n",
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("mentions \"BREAKING\"") == true })
    }

    fun `test the warning underlines the whole release header, not just its first token`() {
        // "## [1.3.0]" is the header the parser matches (the date after it is not part of it). In a real IDE the
        // Markdown PSI splits it and the first leaf is only "##"; the range must not depend on that.
        myFixture.configureByText("CHANGELOG.md", "## [1.3.0] - 2026-09-24\nBREAKING: removed the old API.\n\n## [1.2.0]\nAdded a feature.\n")
        val warning = myFixture.doHighlighting().single { it.description?.contains("mentions \"BREAKING\"") == true }
        assertEquals(0, warning.startOffset)
        assertEquals("## [1.3.0]".length, warning.endOffset)
    }

    fun `test the inspection has a description page instead of "Under construction"`() {
        val name = SemverBumpMismatchInspection().shortName
        assertNotNull("inspectionDescriptions/$name.html", javaClass.classLoader.getResource("inspectionDescriptions/$name.html"))
    }

    fun `test each mismatch is reported exactly once`() {
        myFixture.configureByText("CHANGELOG.md", "## [1.3.0]\nBREAKING: removed the old API.\n\n## [1.2.0]\nAdded a feature.\n")
        assertEquals(1, myFixture.doHighlighting().count { it.description?.contains("SemVer expects a MAJOR bump") == true })
    }

    fun `test only the base-language root of a multi-root file reports`() {
        // A Markdown CHANGELOG.md in a real IDE has more than one PSI root; checkFile runs for each of them.
        val base = com.intellij.openapi.fileTypes.PlainTextLanguage.INSTANCE
        assertTrue(SemverBumpMismatchInspection.isBaseRoot(base, base))
        assertFalse(SemverBumpMismatchInspection.isBaseRoot(com.intellij.lang.Language.ANY, base))
    }

    fun `test headerRange keeps the full header and rejects a range outside the file`() {
        assertEquals(com.intellij.openapi.util.TextRange(5, 28), SemverBumpMismatchInspection.headerRange(5, 23, 100))
        assertNull(SemverBumpMismatchInspection.headerRange(90, 23, 100))
        assertNull(SemverBumpMismatchInspection.headerRange(-1, 3, 100))
        assertNull(SemverBumpMismatchInspection.headerRange(5, 0, 100))
    }

    fun `test a real major bump produces no warning`() {
        myFixture.configureByText(
            "CHANGELOG.md",
            "## [2.0.0]\nBREAKING: removed the old API.\n\n## [1.2.0]\nAdded a feature.\n",
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("mentions \"BREAKING\"") == true })
    }

    fun `test a non-CHANGELOG-md file is never checked, even with the same content`() {
        myFixture.configureByText(
            "notes.md",
            "## [1.3.0]\nBREAKING: removed the old API.\n\n## [1.2.0]\nAdded a feature.\n",
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("mentions \"BREAKING\"") == true })
    }

    fun `test a Removed-section-only mismatch names the Removed section, not the word BREAKING`() {
        myFixture.configureByText(
            "CHANGELOG.md",
            "## [1.3.0]\n### Removed\n- `oldMethod()`.\n\n## [1.2.0]\nAdded a feature.\n",
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("### Removed") == true })
        assertTrue(highlights.none { it.description?.contains("mentions \"BREAKING\"") == true })
    }

    fun `test a non-breaking release with only a minor bump produces no warning`() {
        myFixture.configureByText(
            "CHANGELOG.md",
            "## [1.3.0]\nThis is a non-breaking change.\n\n## [1.2.0]\nAdded a feature.\n",
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("SemVer expects a MAJOR bump") == true })
    }
}
