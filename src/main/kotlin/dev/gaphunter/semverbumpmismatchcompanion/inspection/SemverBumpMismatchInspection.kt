package dev.gaphunter.semverbumpmismatchcompanion.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.gaphunter.semverbumpmismatchcompanion.detect.SemverBumpChecker
import dev.gaphunter.semverbumpmismatchcompanion.parse.ChangelogParser

/**
 * Flags a CHANGELOG.md release entry whose own body text reads as a
 * breaking change (contains "BREAKING") but whose version, versus the
 * release right before it, only bumped MINOR or PATCH -- a real,
 * common mistake: bumping the version by copy-pasting the previous
 * bump's shape without checking whether this release's actual content
 * warrants a MAJOR bump under SemVer.
 */
class SemverBumpMismatchInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val virtualFile = file.virtualFile ?: return null
        if (virtualFile.name != "CHANGELOG.md") return null

        val entries = ChangelogParser.parse(file.text)
        if (entries.isEmpty()) return null

        val hits = SemverBumpChecker.findMismatches(entries)
        if (hits.isEmpty()) return null

        val problems = hits.mapNotNull { hit ->
            val anchor = leafElementAt(file, hit.entry.headerStartOffset) ?: return@mapNotNull null
            val anchorStart = anchor.textRange.startOffset
            val relativeRange = TextRange(
                (hit.entry.headerStartOffset - anchorStart).coerceAtLeast(0),
                (hit.entry.headerStartOffset + hit.entry.headerLength - anchorStart).coerceAtMost(anchor.textLength),
            )
            if (relativeRange.startOffset >= relativeRange.endOffset) return@mapNotNull null

            manager.createProblemDescriptor(
                anchor,
                relativeRange,
                "Release ${hit.entry.version} mentions \"BREAKING\" but only bumped minor/patch versus ${hit.previousVersion} -- SemVer expects a MAJOR bump for a breaking change",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
            )
        }

        return if (problems.isEmpty()) null else problems.toTypedArray()
    }

    /** Leaf-anchored, never a composite node (`SDK_GOTCHAS.md` §20). */
    private fun leafElementAt(file: PsiFile, startOffset: Int): PsiElement? {
        if (startOffset < 0 || startOffset >= file.textLength) return null
        var element = file.findElementAt(startOffset) ?: return file
        while (element.firstChild != null) element = element.firstChild
        return element
    }
}
