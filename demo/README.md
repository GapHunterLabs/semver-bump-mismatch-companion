# Demo data for screenshots

`CHANGELOG.md` — a realistic SDK changelog where `1.4.1` renamed a
public method and changed its return type (a real breaking change) but
only bumped the patch digit versus `1.4.0`. `1.4.2` — added in
0.3.0 — says its change is "non-breaking"; it must NOT be flagged even
though the word "breaking" appears in its body.

## How to get the screenshot

1. `./gradlew runIde` from `semver-bump-mismatch-companion`, open this
   `demo/` folder as the project.
2. Full Screen, open `CHANGELOG.md` — an inline warning should appear
   on the `## [1.4.1]` header, and NOT on `## [1.4.2]`.
3. Screenshot with the warning visible, save into
   `semver-bump-mismatch-companion/docs/screenshots/`. Close the
   sandbox.
