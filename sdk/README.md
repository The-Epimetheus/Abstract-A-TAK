# ATAK SDK jars (per-version)

Drop the ATAK API jar for each supported version here, named by API version
(`versions.json` is the authoritative list — `just list-versions` prints it):

```
sdk/main-4.10.0.jar     # ATAK 4.10 SDK  -> used by the atak410 flavor
sdk/main-5.0.0.jar      # ATAK 5.0 SDK   -> used by the atak500 flavor
...                     # one per row in versions.json ...
sdk/main-5.5.1.jar      # note: patch versions keep their full number
sdk/main-5.8.0.jar      # ATAK 5.8 SDK   -> used by the atak580 flavor
```

Where to get them: each official ATAK SDK release (tak.gov) ships a `main.jar` —
rename per the scheme above. If you have the local `HelloWorld_Collection/` mirror,
`just sync-sdk` stages all of them (plus `keystore/android_keystore`) automatically.

These are wired as **per-flavor `compileOnly`** dependencies in `app/build.gradle`
(generated from the `supportedAtakVersions` list — one loop, no per-version drift).
`compileOnly` means: the API is on the compile classpath but is NOT packaged into
the plugin APK — ATAK provides it at runtime. This is what "treat ATAK as a
library" means concretely.

To add support for a new ATAK version:
1. add a row to `versions.json` and a line to `supportedAtakVersions` in
   `app/build.gradle` (the `compileOnly` wiring picks it up automatically),
2. drop `sdk/main-<api>.jar` here,
3. build it — if the compiler surfaces an API break, see the compatibility-band
   map in `app/build.gradle` (and `MIGRATION.md`, "Adding a new ATAK version").

No new branch. No source duplication.

> If you instead use the takdev devkit (set `takrepo.url`/credentials in
> `local.properties`), takdev resolves the SDK for you and these jars are not
> needed — but takdev provisions a single version per build, so the explicit
> per-flavor jars here are the mechanism that lets one project target many
> versions at once.

These jars are intentionally git-ignored (large, licensed). Keep them local.
