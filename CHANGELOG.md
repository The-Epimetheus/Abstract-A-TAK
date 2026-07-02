# Changelog

All notable changes to this project are documented here.
The format follows Keep a Changelog; versions are managed with bump.

## [Unreleased]

## [v1.1.0] - 2026-07-02

### Changed

- **One seam for version divergence**: the compatibility-band compat helpers are
  now internals behind five new Creators (`LocationCreator`, `VideoCreator`,
  `ImportCreator`, `MenuCreator`, `LayerDownloadCreator`); each selfCheck
  executes its banded API, so a wrong band binding fails at plugin load instead
  of first use. `src/main` no longer names any banded class.
- **First Controller carve** (Humble Object): `features/route/` adds
  `RouteCreator` + the ATAK-free `RouteController`, the `RouteNavPort` callback
  port and `RoutePointSpec` DTO; `RouteEventListener` removed.
- **Single registration point**: `CreatorRegistry` deleted; `CreatorModule` is
  the one place impls are named, with typed accessors on `PluginGraph` and one
  documented composition root (the hand-wired variant is a MIGRATION.md snippet).
- **Band map is data**: `versions.json` now carries `bandPairs`; gradle derives
  the flavors and band source-set wiring from it. Adding an ATAK version is one
  JSON row + one SDK jar. `just list-versions` prints the resolved map.
- **First typed Handles, callback ports and ShellProbe**: `RouteHandle`,
  `LayerDownloadHandle`, `RouteNavPort`, `LayerDownloadPort`, and
  `NavigationStackShellProbe` (constructs + disposes the lazily-created
  nav-stack shell at load).

### Fixed

- `just check-boundary` no longer counts the plugin's own
  `com.atakmap.android.helloworld.*` imports as ATAK SDK touches.
- Plugin package context has no usable data dir (caught live by the systems
  check); file needs now use the host MapView context — documented in
  MIGRATION.md.

Verified live on ATAK 5.3.0.13: systems check
`9 items — FULL=5 PARTIAL=4 SKIPPED=0 FAILED=0`; all 10 versions × CIV debug
plus a 5.3 R8 release build green.

## [v1.0.0] - 2026-07-01

- ci: add release build config and graceful SDK-secret handling
- Abstract-A-TAK: one branch builds every ATAK version
