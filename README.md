# helloworld-unified

TAK's official `helloworld` plugin, restructured so **one branch builds for every
ATAK version** — no more `helloworld-maintenance-4.10` / `-5.0` / … branches.

Verified state: **10 ATAK versions (4.10 → 5.8) × 20 distro flavors = 200 APKs**
build green from this single tree, release (R8) included, and the plugin's
load-time systems check passes on a real ATAK device.

**→ Read [`MIGRATION.md`](MIGRATION.md)** for how it works: what actually changed
between ATAK versions (compiler-verified, not guessed), and the tiered mechanism
that absorbs it. **→ [`CONTEXT.md`](CONTEXT.md)** is the project glossary;
**[`docs/adr/`](docs/adr/)** records the key decisions.

## Quick start

1. **Get the ATAK SDK jars** — one `main.jar` per version you target, from the
   official ATAK SDK releases (tak.gov). Name them `sdk/main-<version>.jar`
   (`sdk/main-5.3.0.jar`, `sdk/main-5.5.1.jar`, …). See [`sdk/README.md`](sdk/README.md).
   Also copy `android_keystore` from any SDK bundle into `keystore/`.
   *(If you have the local `HelloWorld_Collection/` mirror, `just sync-sdk` stages
   all of this for you.)*
2. **See what's supported:** `just list-versions` (reads `versions.json`, the single
   source of truth).
3. **Build:**
   ```bash
   just build                # every version x every flavor (200 APKs)
   just build 5.3            # ATAK 5.3, all flavors
   just build 5.3 civ        # ATAK 5.3, CIV only
   just build all civ        # every version, CIV only
   ```
   (Without `just`: `./gradlew assembleAtak530CivDebug` etc. — any of the 10
   versions works the same way.)
4. **Adding a new ATAK version:** one row in `versions.json`, one jar in `sdk/` —
   then build it. The gradle flavors AND the band wiring are derived from
   `versions.json`, so the new version automatically lands on the correct side of
   every existing compatibility band. Only if the compiler surfaces a genuinely NEW
   API break do you add a `bandPairs` entry + its two source dirs. No branch.

## Layout

```
versions.json            single source of truth: supported versions AND band pairs
                         (gradle derives flavors + band source-set wiring from it;
                         `just list-versions` prints the resolved map)
justfile                 build/list/sync-sdk/check-boundary recipes
app/src/main/            shared plugin code + the ATAK-free abstraction layer:
                         features/<name>/ = Creator interface + DTOs + callback
                         Ports + typed Handles (+ RouteController, the first
                         ATAK-free Controller); abstraction/ = the core contracts
app/src/atakShared/      Creator impls stable across ALL versions + the ONE
                         registration point (CreatorModule/PluginGraph) — the only
                         place ATAK types belong long-term
app/src/bands/atakPre53|atak53plus|...   compatibility-band source sets — one side of a
                         real API break each; identical class names per pair, exactly
                         one compiled per APK; internals BEHIND Creator seams, so
                         each banded binding is verified at load by its selfCheck
app/src/atak410/res/     the one per-version resource delta (two 4.10 drawables)
sdk/                     drop main-<version>.jar files here (gitignored)
docs/adr/                decision records; docs/analysis/ = the evidence
```

## Releases

Each release attaches the **CIV debug APK for every supported ATAK version** — grab
the one matching your device's ATAK version and sideload it. The ATAK SDK jars are
licensed and can't be redistributed, so these are built locally (`just build all civ`)
and uploaded to the release; to build your own, supply `sdk/main-<version>.jar` from
the official ATAK-CIV releases first (see [`sdk/README.md`](sdk/README.md)).

[`.github/workflows/release.yml`](.github/workflows/release.yml) is provided as a
**template** for CI-driven multi-version builds: on a published release it builds and
attaches the CIV APKs *if* you can supply the SDK jars to CI via the
`ATAK_SDK_BUNDLE_URL` secret (a URL to a `.tar.gz` of `sdk/main-*.jar`). If that
secret isn't set it skips with a warning. This repo can't host the jars, so its own
release APKs are attached directly — but the workflow shows the pattern for projects
that can.

Note: `.gitlab-ci.yml` targets TAK's internal CI and predates this restructure —
adapt or remove it for your own project.

---

### Original helloworld notes

helloworld is a complete skeleton project that can be used as a starting point
for developing ATAK private plugins. Private plugins offer the most capability
for utilizing the ATAK subsystem, but this interface will likely change from
version to version — which is exactly what the single-branch restructure above
is designed to absorb.

When constructing the plugin, there are two `android.content.Context` in play:
- the **plugin context** resolves resources from the plugin APK;
- the **mapView context** is used for graphic access (AlertDialogs, Toasts, etc).

Note: using the plugin context to construct an AlertDialog causes a runtime error.
