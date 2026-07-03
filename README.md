# helloworld-unified

This is a teaching version of TAK's `helloworld` ATAK plugin. It shows one idea:

**If you keep your ATAK code separate from the rest of your plugin, you can build
one codebase for many ATAK versions. You do not need a separate branch for each
version.**

The goal here is to teach that structure. The plugin still shows off a lot of ATAK
features, but the point is not the features. The point is the way the project is put
together, so you can copy the pattern into your own plugin.

## The problem this solves

ATAK's plugin API changes from one version to the next. A class gets renamed. A
method gets a new argument. A helper moves to a new package. If your plugin calls
those APIs directly, code that builds against ATAK 5.0 may fail to build against
ATAK 5.8.

The common way to deal with this is to keep one branch per ATAK version, such as
`helloworld-maintenance-5.0` and `helloworld-maintenance-5.8`. Every time you add a
feature or fix a bug, you have to copy that change into each branch. This is slow,
and it is easy to let the branches drift apart.

## The idea

Split your plugin into two parts.

1. Code that talks to ATAK. Keep all of it in one place.
2. Code that holds your plugin's own logic. Keep ATAK out of it.

Most of your plugin is part 2, and part 2 does not change between ATAK versions. When
a new ATAK version renames a class or changes a method, you only touch part 1. You
fix it once, in one branch, and you still build for every version.

In this project:

- Part 2 lives in `app/src/main`. It has no ATAK imports at all. It uses plain Java,
  Android, and small interfaces that the project owns.
- Part 1 lives in `app/src/atakShared`. This is the only place ATAK types are
  allowed. Each wrapper class hides one piece of ATAK behind a simple interface that
  part 2 can call.

A helper command, `just check-boundary`, checks that no ATAK imports slip into
`app/src/main`. That keeps the two parts honest as the plugin grows.

## When two versions really do differ

Most ATAK changes do not reach your code, because the wrappers absorb them. But
sometimes a change is a true break: the same call has to be written two different
ways for two ranges of versions. For example, a metadata store was read one way
through ATAK 5.2 and a different way from 5.3 on.

For those cases the project uses "band" source sets under `app/src/bands`. A band
holds one version range's way of making a call. The build picks the right band for
each version, so exactly one version of the code is compiled into each APK. This is
the only place version-specific code lives, and it stays small. Today the whole
project has only a few bands.

## How the build knows the versions

`versions.json` is the one list of supported ATAK versions. The build reads it and
creates one build flavor per version. It also reads the band ranges from the same
file, so a new version automatically lands on the correct side of every existing
band.

To add an ATAK version you add one line to `versions.json` and drop its SDK jar into
`sdk/`. Then you build it. You only add band code if the compiler shows you a real
new break. You never cut a branch.

## Quick start

You build this like any other ATAK plugin. The Gradle wrapper is included, so you do
not need to install Gradle. You do need the ATAK devkit plugin: set up the ATAK devkit,
or point `takdev.plugin` in `local.properties` at your copy of `atak-gradle-takdev.jar`,
which comes with the ATAK SDK.

1. Get the ATAK SDK jars. You need one `main.jar` per version you want to target,
   from the official ATAK SDK releases on tak.gov. Rename each to
   `sdk/main-<version>.jar`, for example `sdk/main-5.3.0.jar`. See
   [`sdk/README.md`](sdk/README.md). Also copy `android_keystore` from any SDK
   bundle into `keystore/`.

2. See what is supported:

   ```
   just list-versions
   ```

3. Build:

   ```
   just build              # every version and flavor
   just build 5.3          # ATAK 5.3, all flavors
   just build 5.3 civ      # ATAK 5.3, CIV only
   just build all civ      # every version, CIV only
   ```

   If you do not use `just`, run Gradle directly, for example
   `./gradlew assembleAtak530CivDebug`.

## Project layout

```
versions.json         the list of supported ATAK versions and band ranges
justfile              simple build, list, and check commands
app/src/main          your plugin's own code, with no ATAK imports
app/src/atakShared    the wrappers around ATAK, shared by every version
app/src/bands         the few spots where ATAK versions truly differ
sdk/                  put the ATAK SDK jars here (kept out of the repo)
```

For a full walk through the real differences between ATAK versions and how the project
absorbs each one, see [`MIGRATION.md`](MIGRATION.md).

## Does it actually work?

Yes. From this single branch the project builds APKs for 10 ATAK versions, 4.10
through 5.8.

When the plugin loads, it runs a short self-check against the real ATAK it is running
on. The self-check exercises each ATAK wrapper and reports whether every one works on
that version. On a real ATAK 5.8 device the self-check passed with no failures, which
tells you the version wiring for that build is correct.

## Releases

Each release attaches the CIV debug APK for every supported ATAK version. Grab the
one that matches your device's ATAK version and sideload it. The ATAK SDK jars are
licensed and cannot be shared, so these APKs are built locally with
`just build all civ` and uploaded to the release. To build your own, supply the SDK
jars first (see [`sdk/README.md`](sdk/README.md)).

[`.github/workflows/release.yml`](.github/workflows/release.yml) is a template for
building all versions in CI. It builds and attaches the CIV APKs if you give CI a way
to fetch the SDK jars, through the `ATAK_SDK_BUNDLE_URL` secret. If that secret is not
set, it skips with a warning.

Note: `.gitlab-ci.yml` targets TAK's internal CI and predates this restructure.
Adapt or remove it for your own project.

## Notes from the original helloworld

`helloworld` is a full skeleton you can copy to start a private ATAK plugin. Private
plugins get the most access to ATAK, but that access changes from version to version,
which is exactly what the structure above is built to handle.

When you build a plugin, there are two `android.content.Context` objects in play:

- The plugin context loads resources from your plugin APK.
- The mapView context is for on-screen work such as dialogs and toasts.

Using the plugin context to build an AlertDialog causes a runtime error, so use the
mapView context for that.
