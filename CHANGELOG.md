<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Semver Bump Mismatch Companion Changelog

## [Unreleased]

## [0.1.1]

### Added

- Review/star CTA: after 10 distinct real findings, a one-time
  notification asks whether to rate the plugin on Marketplace, with a
  permanent "Don't ask again" option. Standard mechanism used
  catalog-wide since 2026-08-24 (`CONSTITUTION.md` §7.2), rolled out
  to this plugin now.

## [0.1.0]

### Added

- Flags a CHANGELOG.md release entry mentioning "BREAKING" whose
  version, versus the release right before it, only bumped
  minor/patch instead of major.
- Releases under major `0` are never flagged.
- 100% plain-text scan of your own CHANGELOG.md, no network calls, no
  telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/semver-bump-mismatch-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/semver-bump-mismatch-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/semver-bump-mismatch-companion/commits/0.1.0
