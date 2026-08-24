# Semver Bump Mismatch Companion

Flags a `CHANGELOG.md` release entry (Keep a Changelog format) whose
own body text reads as a breaking change (contains "BREAKING") but
whose version, versus the release right before it, only bumped MINOR
or PATCH — SemVer expects a MAJOR bump for a breaking change, and this
is a real, easy mistake: bumping the version by copy-pasting the
previous release's shape without checking whether this release's
actual content warrants MAJOR.

## Why it exists

Consumers of a library rely on SemVer to know whether upgrading is
safe — a breaking change hiding behind a minor/patch bump silently
breaks that contract, and nothing today catches this at write time,
only after someone downstream gets burned.

## Why built this way

- **100% plain-text scan of your own CHANGELOG.md** — no external
  changelog/semver library, no network calls. Hand-rolled parser for
  the Keep a Changelog header format, same "small stable line-oriented
  syntax" technique already proven catalog-wide.
- **`0.x.y` releases are never flagged.** SemVer itself treats the
  entire `0.x` line as "anything can break" — a minor/patch bump
  alongside a breaking change there is expected, not a mistake.

## v0.1 scope — stated honestly, not exhaustively

Only looks for the literal word "BREAKING" in the release body (the
marker most real changelogs and Conventional Commits already use) — a
breaking change described without that word isn't caught.

## Usage

Open any `CHANGELOG.md` with 2+ release entries. A real mismatch shows
as an inline warning on the newer release's header.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
