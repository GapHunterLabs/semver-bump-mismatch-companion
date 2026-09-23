package dev.gaphunter.semverbumpmismatchcompanion.detect

import dev.gaphunter.semverbumpmismatchcompanion.model.BreakingSignal
import dev.gaphunter.semverbumpmismatchcompanion.model.ChangelogEntry
import dev.gaphunter.semverbumpmismatchcompanion.model.MismatchHit
import dev.gaphunter.semverbumpmismatchcompanion.model.SemVer

/**
 * Compares each release entry against the one immediately before it
 * (Keep a Changelog order: newest first, so "before" means the next
 * entry in the list) -- flags a release whose own body text reads as a
 * breaking change (the word "BREAKING", case-insensitive and not
 * negated -- see [hasUnnegatedBreakingMention] -- the same marker
 * Conventional Commits/most real changelog conventions already use)
 * but whose version only bumped MINOR or PATCH, never MAJOR.
 *
 * **v0.2 scope, stated honestly:** `[Unreleased]` is never checked (no
 * real version to compare against yet); a `0.x.y` major of `0` is
 * skipped entirely -- SemVer itself treats the entire `0.x` line as
 * "anything can break", so a minor/patch bump alongside a breaking
 * change there is normal, not a mismatch.
 */
object SemverBumpChecker {

    private val BREAKING_WORD = Regex("""\bbreaking\b""", RegexOption.IGNORE_CASE)

    // A changelog very commonly reassures readers with "non-breaking",
    // "no breaking changes", "not breaking", "without breaking
    // changes" -- a plain substring match on "breaking" flags those as
    // if they claimed the opposite. Looks at the ~20 characters right
    // before the match for a negation word ending exactly there (a
    // hyphen or whitespace gap is allowed, e.g. "non-breaking"). Known
    // gap, stated honestly: a negation with a word in between ("not a
    // breaking change") isn't caught -- rarer phrasing, out of scope
    // for this simple a window.
    private val NEGATION_IMMEDIATELY_BEFORE =
        Regex("""\b(no|non|not|without|isn'?t|aren'?t)\b[\s-]*$""", RegexOption.IGNORE_CASE)
    private const val NEGATION_WINDOW = 20

    private fun hasUnnegatedBreakingMention(text: String): Boolean =
        BREAKING_WORD.findAll(text).any { match ->
            val windowStart = (match.range.first - NEGATION_WINDOW).coerceAtLeast(0)
            !NEGATION_IMMEDIATELY_BEFORE.containsMatchIn(text.substring(windowStart, match.range.first))
        }

    // Keep a Changelog's own standard "### Removed" section header --
    // documented there as "for now removed features". A library
    // removing a feature/API is a breaking change by definition, same
    // strength of signal as the literal word "BREAKING", so it's
    // checked the same way. Deliberately NOT "### Deprecated" -- a
    // deprecation warns of a *future* removal without breaking anything
    // yet, so it's never treated as a breaking-change signal here.
    private val REMOVED_SECTION = Regex("""^\s*#{1,4}\s*Removed\b""", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))

    fun findMismatches(entries: List<ChangelogEntry>): List<MismatchHit> {
        val releases = entries.filter { it.version != "Unreleased" }
        val hits = mutableListOf<MismatchHit>()

        for (i in 0 until releases.size - 1) {
            val newer = releases[i]
            val older = releases[i + 1]

            val newerSemVer = SemVer.parse(newer.version) ?: continue
            val olderSemVer = SemVer.parse(older.version) ?: continue
            if (newerSemVer.major == 0) continue

            val signal = when {
                hasUnnegatedBreakingMention(newer.bodyText) -> BreakingSignal.BREAKING_WORD
                REMOVED_SECTION.containsMatchIn(newer.bodyText) -> BreakingSignal.REMOVED_SECTION
                else -> null
            }
            val majorBumped = newerSemVer.major > olderSemVer.major
            if (signal != null && !majorBumped) {
                hits += MismatchHit(newer, older.version, signal)
            }
        }

        return hits
    }
}
