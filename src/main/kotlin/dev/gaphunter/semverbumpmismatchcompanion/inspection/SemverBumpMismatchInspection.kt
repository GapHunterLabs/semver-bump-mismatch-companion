package dev.gaphunter.semverbumpmismatchcompanion.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import dev.gaphunter.semverbumpmismatchcompanion.detect.SemverBumpChecker
import dev.gaphunter.semverbumpmismatchcompanion.model.BreakingSignal
import dev.gaphunter.semverbumpmismatchcompanion.parse.ChangelogParser
import dev.gaphunter.semverbumpmismatchcompanion.review.ReviewPrompt

/**
 * Flags a CHANGELOG.md release entry whose own body text reads as a
 * breaking change (the word "BREAKING", not negated, or a
 * `### Removed` section) but whose version, versus the release right
 * before it, only bumped MINOR or PATCH -- a real, common mistake:
 * bumping the version by copy-pasting the previous bump's shape
 * without checking whether this release's actual content warrants a
 * MAJOR bump under SemVer.
 */
class SemverBumpMismatchInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val virtualFile = file.virtualFile ?: return null
        if (virtualFile.name != "CHANGELOG.md") return null
        // Registered for every language: with the bundled Markdown plugin a CHANGELOG.md has more than one PSI root and
        // checkFile ran once per root, so each mismatch was reported twice. Only the base-language root reports.
        if (!isBaseRoot(file.language, file.viewProvider.baseLanguage)) return null

        val entries = ChangelogParser.parse(file.text)
        if (entries.isEmpty()) return null

        val hits = SemverBumpChecker.findMismatches(entries)
        if (hits.isEmpty()) return null

        val problems = hits.mapNotNull { hit ->
            val range = headerRange(hit.entry.headerStartOffset, hit.entry.headerLength, file.textLength) ?: return@mapNotNull null
            val signalText = when (hit.signal) {
                BreakingSignal.BREAKING_WORD -> "mentions \"BREAKING\""
                BreakingSignal.REMOVED_SECTION -> "has a \"### Removed\" section"
            }
            val problem = manager.createProblemDescriptor(
                file,
                range,
                "Release ${hit.entry.version} $signalText but only bumped minor/patch versus ${hit.previousVersion} -- SemVer expects a MAJOR bump for a breaking change",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
            )
            ReviewPrompt.recordHit(file.project, "${virtualFile.path}:${hit.entry.version}")
            problem
        }

        return if (problems.isEmpty()) null else problems.toTypedArray()
    }

    companion object {
        fun isBaseRoot(language: Language, baseLanguage: Language): Boolean = language == baseLanguage

        /**
         * The whole release header ("## [1.5.0] - 2026-09-24"), in file offsets, anchored to the file itself. It used
         * to be anchored to the first leaf at the header and clipped to it: with the Markdown plugin that every
         * IntelliJ IDE bundles, that leaf is just the "##" marker, so the warning underlined two characters (tests run
         * without Markdown, where the whole file is one leaf, and never saw it). Null if the range falls outside.
         */
        fun headerRange(start: Int, length: Int, textLength: Int): TextRange? {
            val end = start + length
            if (start < 0 || length <= 0 || end > textLength) return null
            return TextRange(start, end)
        }
    }
}
