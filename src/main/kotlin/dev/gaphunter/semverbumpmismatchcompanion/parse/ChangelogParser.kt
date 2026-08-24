package dev.gaphunter.semverbumpmismatchcompanion.parse

import dev.gaphunter.semverbumpmismatchcompanion.model.ChangelogEntry

/**
 * Hand-rolled parser for the Keep a Changelog format
 * (`## [X.Y.Z] - YYYY-MM-DD` or `## [X.Y.Z]` release headers, an
 * optional `## [Unreleased]` at the top) -- no external Markdown
 * library, same "small stable line-oriented syntax" technique as
 * `DockerfileParser`/`GradleSettingsParser` (`CONSTITUTION.md` §6).
 *
 * `[Unreleased]` is parsed but its `version` text is exactly
 * `"Unreleased"` -- callers filter it out before any semver comparison,
 * since it isn't a real released version to compare against.
 */
object ChangelogParser {

    private val HEADER = Regex("""^##\s*\[([^\]]+)\]""", RegexOption.MULTILINE)

    /** Returns every release section found, in file order (Keep a Changelog convention: newest first). */
    fun parse(text: String): List<ChangelogEntry> {
        val headers = HEADER.findAll(text).toList()
        if (headers.isEmpty()) return emptyList()

        return headers.mapIndexed { index, header ->
            val headerLineEnd = text.indexOf('\n', header.range.last).let { if (it == -1) text.length else it }
            val bodyEnd = headers.getOrNull(index + 1)?.range?.first ?: text.length
            val bodyText = if (headerLineEnd < bodyEnd) text.substring(headerLineEnd, bodyEnd) else ""

            ChangelogEntry(
                version = header.groupValues[1].trim(),
                headerStartOffset = header.range.first,
                headerLength = header.value.length,
                bodyText = bodyText,
            )
        }
    }
}
