# Demo data

`CHANGELOG.md` — a realistic SDK changelog where `1.4.1` renamed a
public method and changed its return type (a real breaking change) but
only bumped the patch digit versus `1.4.0`. `1.4.2` — added in
0.3.0 — says its change is "non-breaking"; it must NOT be flagged even
though the word "breaking" appears in its body.

## Trying it by hand

1. `./gradlew runIde` from `semver-bump-mismatch-companion`, open this
   `demo/` folder as the project.
2. Open `CHANGELOG.md`: the `## [1.4.1]` header is underlined with a
   warning, and `## [1.4.2]` is not.

The recorded media (hero GIF, clips and cover) live in `docs/media/`.
