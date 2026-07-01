# Dependency injection via plain Dagger 2, not Hilt

Controllers receive their **Creators** by constructor injection wired with **plain
Dagger 2**: a manually-owned `@Component` built in the plugin lifecycle, with a
`@Module` that `@Provides` each Creator by delegating to the compiled-in per-version
**Creator factory**. `@IntoSet` multibindings collect every Creator into a
`Set<Creator>` the load-time systems check iterates.

## Why not Hilt

An ATAK plugin does not own the Android `Application`/`Activity` — the host ATAK
process does. Hilt's bootstrap (`@HiltAndroidApp` + lifecycle-scoped components)
requires ownership of those Android components and generally cannot initialize
inside a plugin loaded via the host's classloader. Plain Dagger is just generated
factory code with a component we build ourselves, so it has no such dependency.

## Why a framework at all (vs a hand-wired composition root)

A hand-wired root was the simpler, dependency-free alternative and is entirely
viable. Dagger was chosen for the multibinding registry (systems check gets its
`Set<Creator>` for free) and because DI is the idiom most plugin authors modelling
on this example will expect. Cost: annotation processing + proguard/R8 keep rules.
