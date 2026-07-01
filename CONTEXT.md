# helloworld-unified

A reference ATAK plugin: TAK's official `helloworld`, restructured so **one branch
builds for every supported ATAK version**. Its purpose is to be a model other ATAK
plugin developers copy — showing how to isolate version-specific ATAK API calls
behind an abstraction so business logic never touches a versioned ATAK type.

## Language

**ATAK**:
Android Team Awareness Kit — the host application a plugin runs inside. Supplies
the SDK (`main.jar`) a plugin compiles against.
_Avoid_: ATAC (transcription artifact), TAK (that's the broader platform/org).

**ATAK version**:
Which ATAK SDK you compile against to be a plugin of that ATAK line, e.g. `4.10`,
`5.0` … `5.8`. Each has its own `main.jar` and its own `plugin-api` string the host
checks. One APK targets exactly one ATAK version (build-time binding). Orthogonal
to **Flavor**.
_Avoid_: calling the version a "flavor" — flavor means the distro, never the version.

**Flavor**:
Which distro of ATAK a build targets: `CIV`, `MIL`, `AUS`, `NATO`, `SPA`, … The
distribution axis, orthogonal to **ATAK version**. The manifest `plugin-api` is
`com.atakmap.app@<version>.<flavor>`.
_Avoid_: using "flavor" for the ATAK version. (Gradle calls both axes "product
flavors" internally — that is a build-tool word, not our domain word.)

**Plugin**:
A private ATAK add-on APK loaded by the host. This project is itself a plugin.

**Abstraction layer**:
The set of interfaces in `src/main` through which business logic reaches every
ATAK API call that differs across versions. Business logic depends only on these,
never on an ATAK type directly.

**ATAK-free / the boundary**:
The rule that `src/main` imports none of the **ATAK SDK packages** — `com.atakmap.*`,
`gov.tak.*`, `com.atak.*` (and legacy `transapps.*`). `android.*` and `java.*` are
allowed: Android is stable relative to the ATAK churn we defend against, so raw
`Intent`/`View`/`Context` pass straight to **Controllers**; only ATAK-typed values
become **Plugin DTOs**. Checked by `just check-boundary`, which greps `src/main`
imports for the forbidden packages. This is the *destination* rule: the original
plugin code is migrated behind the boundary feature-by-feature, and the check
measures the remaining debt until it reaches zero.

**Plugin DTO**:
A plugin-owned value type (record) that carries an ATAK object's data across the
**boundary** so `src/main` never names the ATAK type. Impls map DTO↔ATAK. Only ATAK
values need one; Android values do not.

**Handle**:
A plugin-owned opaque token (wrapping an int/UUID) that a **Creator** returns for a
live ATAK object the plugin retains (a `Marker`, `Layer`, `DropDown`). `src/main`
holds handles, never the ATAK object; the impl keeps a `Handle → ATAK object`
registry and evicts on delete. A **Plugin DTO** carries *data by value*; a **Handle**
is a *reference to a live object*.

**Creator**:
An interface in the abstraction layer that wraps one cluster of version-sensitive
ATAK interaction (e.g. **CotCreator** wraps CoT-event construction). Lives in
`src/main`. Handles **both directions**: outbound calls (`main` → ATAK) and inbound
event registration, where a Creator method takes a **callback port** and the impl
adapts the ATAK listener to it. Each ATAK version supplies one implementation.
_Avoid_: "wrapper", "adapter", "manager" — pick **Creator** for this role.

**Callback port**:
A plugin-owned listener interface in `src/main` (ATAK-free, delivers **Plugin
DTOs**) that a **Controller** implements to receive inbound ATAK events. A
**Creator** registers it; the impl implements the real ATAK listener and adapts
event → DTO → port.

**Creator implementation**:
A concrete class implementing a **Creator**, the only place ATAK types appear. It
lives in the **shared impl source set** when its ATAK API is stable across all ten
versions, or in a per-version source set when it diverges. The **Creator factory**
binds exactly one impl per APK, so an API removed in a later version can never break
another version's compile.

**Shared impl source set**:
`src/atakShared/java` — holds **Creator implementations** whose ATAK API is
source-stable across *every* targeted version. Added to all ten flavors' source
sets, so a stable impl is written once, not duplicated per version. It is not
`src/main` (it touches ATAK types); the zero-ATAK-in-main rule is unaffected.
_Avoid_: putting an impl here the moment any targeted version changes that API — it
must move to per-version / **compatibility-band** sets then.

**Compatibility band**:
The set of versions over which one **Creator implementation** (or compat helper)
compiles unchanged. Stable-everywhere impls have a band of all ten (→ **shared impl
source set**). A divergence splits the band; each side gets its own source set,
named for the boundary (`src/atakPre53` / `src/atak53plus`, `atakPre55`/`atak55plus`,
`atakPre57`/`atak57plus`, `atakPre58`/`atak58plus`) with identical fully-qualified
class names on each side — exactly one is compiled per APK. The full band→version
map lives in `app/build.gradle`; the byte-level evidence in
`docs/analysis/byte-confirmation.md`.

**Creator factory**:
A per-version class of identical fully-qualified name in each version's source set
that returns the concrete **Creator implementations** for that build. It is how
`src/main` obtains impls without naming an ATAK type or using reflection — exactly
one factory is compiled in per APK.

**Humble shell**:
A thin subclass of an ATAK base type (`DropDownReceiver`, `MapComponent`,
`AbstractPlugin`, `AbstractLayer`, …) that holds **no** plugin logic — it forwards
every ATAK callback to a **Controller** in `src/main` and calls **Creators** for
outbound work. Lives in the **shared impl source set** (or per-version if its base
type changed). The Humble-Object half that must touch ATAK. Exposes a safe
construct+dispose path so the **load-time systems check** can verify it at load.
_Avoid_: "shell" carrying real logic — if it has behavior worth testing, that
behavior belongs in a **Controller**.

**Controller**:
The ATAK-free plugin object in `src/main` that a **Humble shell** forwards to; holds
the plugin's actual behavior and depends only on **Creators** and DTOs. The
Humble-Object half that stays testable and version-independent.
_Avoid_: letting a Controller import any ATAK type.

**Build label**:
`BuildConfig.TARGET_ATAK_VERSION` — the gradle-generated constant naming which ATAK
version this APK was compiled for. It does **not** dispatch logic at runtime (the
compiler already selected the implementation via source sets) — it only labels the
build, e.g. in systems-check log lines.
_Avoid_: a runtime switch/`case` over versions — that is the anti-pattern this whole
project replaces with build-time source-set selection.

**Load-time systems check**:
A sanity check that runs on **every** load (all builds, field included). It walks
every **Creator** and calls its **selfCheck**, **and** constructs+disposes every
**Humble shell** (so a base-class break surfaces at load even for a lazily-created
shell, not on first open), so a version-binding mistake surfaces at startup rather
than at first use. "Not for end users" means **invisible**
— results go to the **log only**, no toast/UI — not that it is gated out of release.
Always-on is safe because irreversible **selfChecks** degrade to best-effort and
local artifacts are created + torn down under a reserved test namespace. Never fatal
to ATAK.
_Avoid_: "health check" as a separate concept — it is this. Do not treat it as a
showcased user feature, and do not read "not for end users" as "debug-only".

**selfCheck**:
A best-effort method every **Creator** implements. It performs the Creator's real
ATAK operation and tears it down (create the CoT, then delete it — teardown in a
`finally`, using a reserved test-artifact namespace). Where the effect is
irreversible/external (CoT already emitted to the network), it degrades: exercise
as much of the real path as is safe, and report the level reached rather than
leaking. Returns a graded **verification level**, never a bare boolean.

**verification level**:
The graded outcome of a **selfCheck**: `FULL` (real op done and undone), `PARTIAL`
(built + API resolved, dispatch not exercised because irreversible, or routed to a
loopback seam), `SKIPPED` (could not safely test), `FAILED` (path threw / ATAK
symbol missing — the signal a wrong version bound).

## Naming convention

One package per feature (`features/<feature>/`) colocating its parts; the role is
carried in the class-name suffix: **`Creator`** (interface), **`Spec`**/**`Dto`**
(Plugin DTO), **`Controller`**, **`Port`** (callback port), **`Handle`**, **`Impl`**
(Creator implementation, in a source set outside `src/main`).

## Example dialogue

**Dev:** ATAK 5.2 gives me `getMapData()` returning `MapData` with `putDouble(...)`,
but 5.3 deleted that class and returns `MetaDataHolder2` with `setMetaDouble(...)` —
won't one branch break on one side or the other?

**Expert:** No. Those are two impls of the same class in two **compatibility band**
source sets: `src/atakPre53` compiles only into the 4.10–5.2 flavors, `src/atak53plus`
only into 5.3+. A given APK binds one, so the deleted symbol is never on its
classpath.

**Dev:** And how do I know the right one got bound, without clicking through the app?

**Expert:** The **load-time systems check**. On load we walk every **Creator**,
invoke its **selfCheck** once against the real host, and log the graded result —
a wrong binding shows up as `FAILED` in the log the moment the plugin loads.
