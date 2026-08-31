package dev.gaphunter.semverbumpmismatchcompanion.detect

import dev.gaphunter.semverbumpmismatchcompanion.model.ChangelogEntry
import dev.gaphunter.semverbumpmismatchcompanion.model.MismatchHit
import dev.gaphunter.semverbumpmismatchcompanion.model.SemVer

/**
 * Compares each release entry against the one immediately before it
 * (Keep a Changelog order: newest first, so "before" means the next
 * entry in the list) -- flags a release whose own body text reads as a
 * breaking change (contains "BREAKING", case-insensitive -- the same
 * marker Conventional Commits/most real changelog conventions already
 * use) but whose version only bumped MINOR or PATCH, never MAJOR.
 *
 * **v0.1 scope, stated honestly:** `[Unreleased]` is never checked (no
 * real version to compare against yet); a `0.x.y` major of `0` is
 * skipped entirely -- SemVer itself treats the entire `0.x` line as
 * "anything can break", so a minor/patch bump alongside a breaking
 * change there is normal, not a mismatch.
 */
object SemverBumpChecker {

    private val BREAKING_MARKER = Regex("breaking", RegexOption.IGNORE_CASE)

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

            val isBreaking = BREAKING_MARKER.containsMatchIn(newer.bodyText) ||
                REMOVED_SECTION.containsMatchIn(newer.bodyText)
            val majorBumped = newerSemVer.major > olderSemVer.major
            if (isBreaking && !majorBumped) {
                hits += MismatchHit(newer, older.version)
            }
        }

        return hits
    }
}
