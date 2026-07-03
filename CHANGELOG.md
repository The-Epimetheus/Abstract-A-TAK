# Changelog

All notable changes to this project are listed here. The format follows Keep a
Changelog, and versions are managed with bump.

## [Unreleased]

### Changed

- Moved every remaining demo button off the old receiver and into its own feature.
  Each feature now has a small interface that wraps ATAK (a Creator) and a plain
  class that holds the logic (a Controller). The old receiver went from about 3,180
  lines to about 500, and it now only handles the dropdown lifecycle.
- Reframed the docs as a teaching project. The README, the glossary, and the
  migration guide now lead with the main idea: keep ATAK code separate from your own
  code so one branch builds for many ATAK versions.

### Removed

- Dropped the per-version resource override folder. The two drawables it held already
  live in the shared resources, so every version, including 4.10, gets them there.

### Verified

- On a real ATAK 5.8 device, the load-time self-check swept 30 wrappers and probes
  with no failures. The instrumented test suite passes on that device as well.

## [v1.1.0] - 2026-07-02

### Changed

- Put version differences behind wrappers. The per-version helpers became internals
  of five new Creators (`LocationCreator`, `VideoCreator`, `ImportCreator`,
  `MenuCreator`, `LayerDownloadCreator`). Each self-check runs its version's API, so a
  wrong version wiring fails at load instead of on first use. `app/src/main` no longer
  names any version-specific class.
- Added the first Controller split. The route feature added `RouteCreator` and the
  ATAK-free `RouteController`, plus a callback interface and a plain point value.
- Made the band map data. `versions.json` now holds the band ranges, and the build
  reads them. Adding an ATAK version is one JSON row and one SDK jar.

### Fixed

- `just check-boundary` no longer counts the plugin's own imports as ATAK imports.
- Found and documented that the plugin's own package has no usable data folder, so
  file work uses the host map context instead.

## [v1.0.0] - 2026-07-01

- Added the release build config with graceful handling when the SDK secret is
  missing.
- First version where one branch builds every ATAK version.
