# How one branch builds for every ATAK version

This is TAK's `helloworld` plugin, set up so a single branch builds for every
supported ATAK version, 4.10 through 5.8, across all distribution flavors. There is
no maintenance branch per version. This page explains how that works and how to add a
new version.

## What really differs between ATAK versions

We started by guessing the differences were only cosmetic. Then we compiled the same
shared code against every SDK, and the compiler proved otherwise. It found real API
breaks at four version boundaries.

| Boundary | The real break |
|---|---|
| 5.0 to 5.1 | `RadioMapComponent.registerControl(View)` was removed. A keyed `(String, View)` version exists in every release. `RasterUtils.getCurrentImagery` was removed. `queryDatasets` exists in every release. |
| 5.2 to 5.3 | The `MapData` class was removed. `MapView.getMapData()` now returns `MetaDataHolder2` with a different API (`putX` became `setMetaX`). `GeocodingTask` was removed, but `GeocodeManager` exists in every release. |
| 5.4 to 5.5 | `LayerDownloader` was redesigned, and its `(MapView)` constructor was removed. The map menu widgets moved to `gov.tak.*` packages. |
| 5.6 to 5.8 | The import framework was redesigned (`ImportInternalSDResolver` became `ImportResolver`). `ConnectionEntry` moved to `gov.tak.api.video`. `ImportMissionV1PackageSort` and `ParseUtils` were removed in 5.8. |

Each difference was checked with `javap` against every version's SDK jar, so these
are confirmed breaks, not guesses.

So the point holds, just more precisely than the first guess. The maintenance
branches existed for build settings plus a small number of local API breaks. Neither
of those needs a branch. Build settings are what build flavors are for. The breaks
are what the bands below are for.

## The structure

```
helloworld-unified/
  app/src/main/          your plugin code plus the ATAK-free interfaces it calls
  app/src/atakShared/    the ATAK wrappers that are the same for every version
  app/src/bands/         the few spots where versions truly differ, one folder per side
  sdk/                   one main-<version>.jar per version (kept out of the repo)
  versions.json          the list of supported versions and the band ranges
  app/build.gradle       reads versions.json and sets up the flavors and bands
```

The build has two flavor dimensions:

- `atakVersion`, such as `atak410` through `atak580`. This picks the SDK jar, the
  plugin-api version string, the `BuildConfig.TARGET_ATAK_VERSION` label, and which
  band folders compile in.
- `application`, such as `civ` or `mil`. This is the distribution axis from the
  original build. The manifest plugin-api combines both, as
  `com.atakmap.app@<version>.<DISTRO>`.

## How a difference gets handled, cheapest way first

1. The API is the same for every version. This is still most of the code. It stays in
   `app/src/main` or `app/src/atakShared` and is shared. There is nothing to do.
2. There is one form that works everywhere. Use it. For example, use the keyed
   `registerControl(String, View)` instead of the removed `(View)` form, or plain
   `Integer.parseInt` instead of the removed `ParseUtils`. No band is needed.
3. There is no shared form, because a class was removed or redesigned. Now you use a
   band: two folders split at the breaking version, with the same class name on each
   side, wired to their versions in `app/build.gradle`. Small breaks get a tiny helper
   class. Larger ones reuse TAK's own per-version code.

A difference is always kept in the smallest possible per-version folder. The larger
shared code is written and tested once.

## The ATAK-free layer and the self-check

Building everywhere is half of the project. The other half is keeping your own code
away from ATAK entirely, so version changes do not reach it.

- Creators are plugin-owned interfaces in `app/src/main/features/<name>/` that wrap
  version-sensitive ATAK work. Their implementations live outside `app/src/main`.
  Every band difference sits behind one of these wrappers, so if the wrong band is
  wired in, that Creator's self-check fails at load.
- Controllers hold your plugin's logic and never import ATAK. A thin shell class
  forwards ATAK events to them. Live ATAK objects the plugin keeps are held as plain
  handle tokens, not as ATAK types.
- Dagger 2 wires the implementations to the code that uses them. `CreatorModule` is
  the one place that lists them. If you do not want Dagger, the same job is a small
  hand-written factory that returns the set of Creators. Keep one approach, not both.
- The load-time self-check runs on every plugin load. Each Creator does its real ATAK
  work and undoes it, and the banded ones run their band's API and log which side was
  wired in. If the wrong version got wired in, it shows up as `FAILED` at load, not
  later when a user taps a button.

We ran this on a real ATAK 5.3 device and the check reported no failures. It has
already caught real problems twice: a self-check that left a required field off a CoT
message, and a file-path bug where the plugin's own package has no usable data
folder, so file work must use the host map context instead.

## Adding a new ATAK version

1. Add one row to `versions.json` and drop `sdk/main-<version>.jar` into `sdk/`. That
   is the whole registration. The build reads the JSON and sets up both the flavor and
   the band wiring, and the new version lands on the correct side of every existing
   band.
2. Build it with `just build <version>`. If it compiles, you are done.
3. If it shows an API break, first look for a form that works everywhere. If there is
   none, add one band-pair entry to `versions.json` and create the two folders. Keep
   the difference behind its feature's Creator, so the new band is checked at load.

## Checking a build

- `just list-versions` shows what is supported.
- `just check-boundary` shows any ATAK imports still left in `app/src/main`.
- On a device, install the APK that matches the host's ATAK version, load the plugin,
  and run `adb logcat -s HelloWorldSystemsCheck`. Each Creator reports `FULL`,
  `PARTIAL`, `SKIPPED`, or `FAILED` at load.

## Notes for this repo's setup

- `HelloWorld_Collection/` (optional, kept out of the repo) is a local copy of TAK's
  per-version maintenance branches and SDK bundles. When it is present, `just sync-sdk`
  stages the jars and keystore from it. The repo works fine without it if you supply
  the SDK jars yourself, as in step 1 of the README quick start.
- `atak-docs/` (optional, kept out of the repo) is a local ATAK javadoc dump used
  during the first round of API analysis.
- For release builds, the takdev `-applymapping` line in `app/proguard-gradle.txt` is
  turned off for standalone builds. See the comment there.
