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
}
