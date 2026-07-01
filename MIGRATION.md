# helloworld-unified — one branch, every ATAK version

This is TAK's official `helloworld` plugin, restructured so a **single branch**
builds for every supported ATAK version (4.10 → 5.8, ×20 distro flavors) instead of
carrying one maintenance branch per version.

## What actually differs between ATAK versions — the evidence

The initial hypothesis (from diffing the 4.10 vs 5.0 maintenance branches) was that
the differences were cosmetic. **Compiling the shared source against every SDK
disproved that** — the compiler is the ground truth, and it found real API breaks at
four version boundaries:

| Boundary | Real API break |
|---|---|
| **5.0 → 5.1** | `RadioMapComponent.registerControl(View)` removed (keyed `(String,View)` overload exists in *all* versions); `RasterUtils.getCurrentImagery` removed (`queryDatasets` exists in all versions) |
| **5.2 → 5.3** | `MapData` class **removed**: `MapView.getMapData()` now returns `MetaDataHolder2` with a different API (`putX` → `setMetaX`); `GeocodingTask` class removed (`GeocodeManager` exists in all versions) |
| **5.4 → 5.5** | `LayerDownloader` redesigned (per-setter config + `startDownload()` → `RequestBuilder`; `(MapView)` ctor removed); map-menu widget system moved to `gov.tak.*` packages |
| **5.6 → 5.7 / 5.7 → 5.8** | Import framework redesigned (`ImportInternalSDResolver` → `ImportResolver`); `ConnectionEntry` moved to `gov.tak.api.video`; `ImportMissionV1PackageSort` and `ParseUtils` removed in 5.8 |

Byte-level evidence (javap against each SDK jar, plus the false alarms that byte-
checking *cleared*): [`docs/analysis/byte-confirmation.md`](docs/analysis/byte-confirmation.md).

The point stands, just sharper than the original claim: the maintenance branches
existed for **build config plus a handful of localized API breaks** — neither needs
a branch. Build config is what product flavors are for; the breaks are what the
tiers below are for.

## The structure

```
helloworld-unified/
├── app/src/main/                 ← the shared plugin (~60 files) + the ATAK-free
│   │                               abstraction layer (features/, abstraction/)
├── app/src/atakShared/java/      ← Creator impls + compat helpers stable across
│   │                               ALL versions (written once)
├── app/src/atakPre53/ + atak53plus/   ┐ compatibility-band source sets — each pair
├── app/src/atakPre55/ + atak55plus/   │ carries the two sides of one real API break;
├── app/src/atakPre57/ + atak57plus/   │ identical class names per pair, exactly one
├── app/src/atakPre58/ + atak58plus/   ┘ side compiled into any APK
├── app/src/atak410/res/          ← the only resource delta (two 4.10 drawables)
├── sdk/                          ← main-<version>.jar per version (gitignored)
├── versions.json                 ← single source of truth for supported versions
└── app/build.gradle              ← two flavor dimensions + the band→flavor map
```

Two Gradle flavor dimensions:

* **`atakVersion`** = `atak410` … `atak580` — selects the SDK jar (`compileOnly`),
  the `plugin-api` version string, `BuildConfig.TARGET_ATAK_VERSION`, and which
  band source sets compile in.
* **`application`** = `civ` | `mil` | … — the distro axis the official build already
  had, untouched. The manifest `plugin-api` composes both:
  `com.atakmap.app@<version>.<DISTRO>` (verified against merged manifests for all 10
  versions).

## How divergence is handled, cheapest tier first

1. **API identical across versions** (still the overwhelming majority) → code stays
   in `src/main` or `src/atakShared`, shared. Nothing to do.
2. **A universal form exists** → call it. Examples: the keyed
   `registerControl(String,View)` instead of the removed `(View)` overload;
   `queryDatasets` instead of the removed `getCurrentImagery`; plain
   `Integer.parseInt` instead of the removed `ParseUtils`. No band needed.
3. **No universal form** (class removed/redesigned) → a **compatibility band**: two
   source sets split at the breaking boundary, same fully-qualified class name on
   each side, wired to their flavors in `app/build.gradle` (see the band map there).
   Small breaks get a tiny helper (`MockLocationApplier`, `LayerDownloadHelper`,
   `VideoConnectionCompat`, `MissionImportCompat`); whole-class rewrites reuse TAK's
   own per-version code (`MenuFactory`, `HelloImportResolver`).
4. **Resource/asset differs** → per-version `src/<flavor>/res` override (the 4.10
   drawables).

Divergence is always confined to the smallest possible per-version source set; the
expensive shared code is written and tested once.

## The abstraction layer (and the self-test)

Beyond building everywhere, the project demonstrates *insulating business logic from
ATAK entirely* (see [`CONTEXT.md`](CONTEXT.md) and [`docs/adr/0002`](docs/adr/0002-zero-atak-types-in-main.md)):

* **Creators** — plugin-owned interfaces in `src/main/.../features/<name>/` wrapping
  version-sensitive ATAK interaction; impls live outside `src/main`
  (`CotCreator`/`RadioCreator` are the worked examples; the rest of the legacy code
  migrates feature-by-feature — `just check-boundary` measures the remaining debt).
* **Dagger 2** wires impls to consumers via `@IntoSet` multibindings
  ([`docs/adr/0003`](docs/adr/0003-dagger2-not-hilt.md)); `CreatorRegistry` shows the
  same wiring dependency-free if you don't want DI.
* **Load-time systems check** — on every plugin load, each Creator's `selfCheck()`
  performs its real ATAK operation and tears it down, logging a graded result
  (tag `HelloWorldSystemsCheck`). A wrong version binding surfaces at load as
  `FAILED`, not at first use in the field. Verified live on ATAK 5.3:
  `FULL=2 PARTIAL=0 SKIPPED=0 FAILED=0`.

## Adding a new ATAK version

1. Add one row to `versions.json` and one line to `supportedAtakVersions` in
   `app/build.gradle`; drop `sdk/main-<ver>.jar`.
2. Build it: `just build <ver>`. **The compiler is the oracle** — if it's green,
   you're done.
3. If it surfaces an API break: prefer a universal form (tier 2); otherwise add the
   new version to the matching side of each band map entry, and split a new band
   only for the genuinely new break (tier 3).

## Verifying a build

* `just list-versions` — what's supported.
* `just check-boundary` — ATAK imports remaining in `src/main` (migration debt).
* On device: install the APK matching the host's ATAK version, load the plugin, and
  `adb logcat -s HelloWorldSystemsCheck` — every Creator reports
  `FULL`/`PARTIAL`/`SKIPPED`/`FAILED` at load.

## Notes for this repo's development setup

* `HelloWorld_Collection/` (gitignored, optional) is a local mirror of TAK's
  per-version maintenance branches + SDK bundles. When present, `just sync-sdk`
  stages the jars/keystore from it, and the band impls that reuse TAK's own
  per-version code were sourced from it. The repo is fully usable without it —
  supply the SDK jars yourself (step 1 of the README quick start).
* `atak-docs/` (gitignored, optional) is a local ATAK javadoc dump used during the
  original API analysis.
* Release builds: the takdev `-applymapping` line in `app/proguard-gradle.txt` is
  disabled for standalone (non-devkit) builds — see the comment there.
