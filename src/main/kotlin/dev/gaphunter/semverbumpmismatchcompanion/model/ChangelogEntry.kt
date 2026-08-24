package dev.gaphunter.semverbumpmismatchcompanion.model

/** One `## [X.Y.Z]` release section of a Keep a Changelog-format CHANGELOG.md: the version string, where its header starts, and the body text up to (not including) the next header. */
data class ChangelogEntry(
    val version: String,
    val headerStartOffset: Int,
    val headerLength: Int,
    val bodyText: String,
)

/** A parsed semantic version -- only major/minor/patch, no pre-release/build metadata (v0.1 scope). */
data class SemVer(val major: Int, val minor: Int, val patch: Int) {
    companion object {
        private val PATTERN = Regex("""^(\d+)\.(\d+)\.(\d+)""")

        fun parse(text: String): SemVer? {
            val match = PATTERN.find(text) ?: return null
            val (major, minor, patch) = match.destructured
            return SemVer(major.toInt(), minor.toInt(), patch.toInt())
        }
    }
}

/** One finding: a release whose own body text reads as a breaking change, but whose version bump versus the previous release didn't increment MAJOR. */
data class MismatchHit(val entry: ChangelogEntry, val previousVersion: String)
