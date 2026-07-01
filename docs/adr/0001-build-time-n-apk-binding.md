# Build-time version binding: N APKs, per-flavor impls, no reflection

Each ATAK version is a Gradle flavor that produces its own APK compiled against
only that version's SDK; the correct **Creator implementation** is selected by
*which source set compiled in*, not by any runtime branch. We reject a single
universal APK that detects the host version and dispatches reflectively.

## Considered options

- **Runtime binding (one universal APK + reflection).** Rejected: ATAK pins
  `plugin-api` per host, so a mismatched APK usually won't load anyway; and
  reflection throws away compile-time safety and wraps every call site in
  try/catch for no gain here.
- **In-code `enum` switch across versions.** Rejected: physically can't compile
  once an API is *removed* (e.g. `CotMessage.builder()` gone in 5.3) — the removed
  symbol isn't on the newer SDK's classpath.
- **Build-time per-flavor impls (chosen).** Each APK sees only its own version's
  API, so removed/changed symbols in other versions can never break its compile.

## Consequences

Nothing dispatches on the version at runtime — `BuildConfig.TARGET_ATAK_VERSION`
only labels the build (e.g. in systems-check logs). Adding a version is one
`versions.json` row + one `supportedAtakVersions` line + one SDK jar (plus checking
the compatibility-band map in `app/build.gradle` if the new version breaks an API),
never a branch.
