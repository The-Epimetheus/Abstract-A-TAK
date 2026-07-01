# javap byte-confirmation (post-analysis)

The analysis agents could not run `javap`, so they inferred signatures from source +
javadoc and flagged several items as unconfirmed. Confirmed here against each
version's real `main.jar` with JDK 17 `javap`.

## Risk items — all confirmed STABLE (no band)

| Item | Finding |
|---|---|
| `SensorFOV.setMetrics` | `(float,float,float)` in **all** versions (agents' "int" was wrong). 5.8 adds an extra overload; base is stable. |
| `GLMapView` fields (`scratch`, `idlHelper`, `currentPass`, `RENDER_PASS_SPRITES`) | Identical across 4.10/5.0/5.1/5.8. Stable. |

## The two "banded" creators — bands ELIMINATED

Byte-confirmation shows only the *old overloads* were removed; the newer forms
existed in every version. Targeting the universal overload makes both `stable-all`.

| Creator | Old (removed at 5.1) | Universal overload (present 4.10→5.8, all 10 verified) |
|---|---|---|
| `RadioCreator` | `RadioMapComponent.registerControl(View)` / `unregisterControl(View)` | `registerControl(String,View)` / `unregisterControl(String)` |
| `LayerDownloadCreator` | `RasterUtils.getCurrentImagery(MapView,GeoBounds): List<ImageDatasetDescriptor>` | `RasterUtils.queryDatasets(GeoBounds,boolean): List<DatasetDescriptor>` (impl filters to imagery) |

## Compiler ground-truth (atak530 build) — agents UNDER-reported 5.1+ divergence

Compiling the unified `src/main` against the **5.3** SDK (the tablet's version)
surfaced divergences the agent inventories missed (their 5.1–5.8 inventories were
thin/projected). The **compiler is the ground truth.** True 5.3 divergence set
(27 errors, 4 causes):

| # | Symbol | Divergence | Fix |
|---|---|---|---|
| 1 | `com.atakmap.android.maps.MapData` | **class present in 5.0, REMOVED by 5.3** (used in `HelloWorldDropDownReceiver`) | genuine Creator + per-version/band handling |
| 2 | `com.atakmap.android.user.geocode.GeocodingTask` | **class present in 5.0, REMOVED by 5.3** (used in 3 `speechtotext` classes); geocode now via `GeocodeManager`/`GeocodeConverter` | genuine Creator + per-version handling |
| 3 | `RadioMapComponent.registerControl(View)` | old overload removed in 5.1; `(String,View)` is universal | RadioCreator → universal overload |
| 4 | `RasterUtils.getCurrentImagery` + `LayerDownloadExample` `Callback.onRegionSelectFinished` | 5.1 break; `queryDatasets` universal | LayerDownloadCreator → universal overload |

Lesson: the javadoc/source analysis is a starting map; **every `stable-all` claim must
be compiler-verified per version** before `src/atakShared` is trusted. Items 1–2 mean
some Creators are genuinely banded (not zero bands after all) — the abstraction/
per-version source sets are needed for real, exactly as designed.

## Further compiler ground-truth (5.5.1–5.8 builds)

Building the remaining versions surfaced additional real divergences (again missed
by the javadoc analysis):

| Boundary | Divergence |
|---|---|
| 5.4 → 5.5 | `LayerDownloader` redesigned: per-setter config + `startDownload()` → `RequestBuilder` + `startDownload(rb)`; `LayerDownloader(MapView)` ctor removed (5.2–5.4 have both ctors; ≤5.1 have `MapView` only). Map-menu widget system moved `com.atakmap.android.widgets` → `gov.tak.platform/api.widgets` (`MapMenuButtonWidget` submenu API, `setOnClickAction` → `OnButtonClickHandler`). |
| 5.6 → 5.7 | Import framework redesigned: `ImportInternalSDResolver` (base of `HelloImportResolver`) removed → extend `ImportResolver`; `ConnectionEntry` moved `com.atakmap.android.video` → `gov.tak.api.video`. |
| 5.7 → 5.8 | `ImportMissionPackageSort.ImportMissionV1PackageSort` removed (TAK's own 5.8 HelloWorld dropped that demo path); `com.atakmap.android.util.ParseUtils` removed. |

## Net result (final — supersedes the earlier "zero bands" conclusion)

The initial "zero compatibility bands required" conclusion was **wrong** — it was
based on the javadoc analysis plus the two universal-overload fixes, before the
compiler had been run against every SDK. The final, compiler-verified reality:

- **Universal-API fixes (no band needed):** Radio `registerControl(String,View)`,
  `RasterUtils.queryDatasets`, `LayerDownloader.create` via `getContext()` — *(that
  last one turned out wrong too and became part of the 5.5 band)* — and `ParseUtils`
  replaced by plain Java.
- **Four real compatibility bands** carry everything that has no universal form:
  `atakPre53/atak53plus` (MapData → MetaDataHolder2), `atakPre55/atak55plus`
  (LayerDownloader + menu-widget system), `atakPre57/atak57plus` (import framework +
  ConnectionEntry), `atakPre58/atak58plus` (ImportMissionV1PackageSort).
- Resource delta: the two `atak410` drawables (unchanged from the original analysis).

Verified end-state: all 10 versions × 20 distros (200 APKs) compile green from one
branch, and the release (R8) build + on-device systems check pass on real ATAK 5.3.
