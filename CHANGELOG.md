<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Semver Bump Mismatch Companion Changelog

## [Unreleased]

## [0.3.0]

### Fixed

- A release that explicitly says its change is NOT breaking --
  "non-breaking", "not breaking", "no breaking changes", "without
  breaking", "isn't/aren't breaking" -- is no longer flagged. The
  detector previously matched the bare substring "breaking" anywhere
  in the body, so a changelog reassuring readers that nothing broke
  was read as if it claimed the opposite. Confirmed with 2 tests that
  failed against the 0.2.0 detector before the fix.
- The Marketplace description now mentions the `### Removed` section
  signal added in 0.2.0 -- it was shipped but never added to the
  published description.
- "Rate on Marketplace" now links to this plugin's own reviews page
  instead of the vendor page.

## [0.2.0]

### Added

- Detects a `### Removed` section (Keep a Changelog's own standard
  header for removed features) as a breaking-change signal, alongside
  the existing literal "BREAKING" word check -- a release removing a
  feature is breaking by definition, even without the word appearing
  anywhere. `### Deprecated` is deliberately never treated this way.

## [0.1.1]

### Added

- Review/star CTA: after 10 distinct real findings, a one-time
  notification asks whether to rate the plugin on Marketplace, with a
  permanent "Don't ask again" option. Standard mechanism used
  catalog-wide since 2026-08-24, rolled out
  to this plugin now.

## [0.1.0]

### Added

- Flags a CHANGELOG.md release entry mentioning "BREAKING" whose
  version, versus the release right before it, only bumped
  minor/patch instead of major.
- Releases under major `0` are never flagged.
- 100% plain-text scan of your own CHANGELOG.md, no network calls, no
  telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/semver-bump-mismatch-companion/compare/0.3.0...HEAD
[0.3.0]: https://github.com/GapHunterLabs/semver-bump-mismatch-companion/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/GapHunterLabs/semver-bump-mismatch-companion/compare/0.1.1...0.2.0
[0.1.1]: https://github.com/GapHunterLabs/semver-bump-mismatch-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/semver-bump-mismatch-companion/commits/0.1.0
