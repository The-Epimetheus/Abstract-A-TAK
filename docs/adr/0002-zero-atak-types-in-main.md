# src/main is 100% ATAK-type-free; impls shared via a dedicated source set

`src/main` (business logic, Creator interfaces, plugin-owned DTOs, the factory
interface, the enum) references **no** ATAK type — `com.atakmap.*` / `gov.tak.*`
appear only inside **Creator implementations**, which trade in plugin-owned DTOs
across the seam. This maximizes insulation: because you can't predict what TAK
changes next, the entire ATAK surface HelloWorld touches is wrapped now, so a
future break is "add one impl," never "refactor business logic."

## Why not the cheaper alternatives

- **ATAK types allowed in `main` when "stable"** (the pre-existing MIGRATION.md
  shape): rejected — a type judged stable that later changes forces a refactor
  across the seam, defeating the point.
- **Default impl in `main`, override per-flavor**: rejected — and in fact
  impossible for Java. Android's override-priority merging exists only for
  *resources*; Java source dirs are merged additively, so a default copy plus an
  override copy of the same FQN is a duplicate-class compile error. A diverging
  class must be absent from the shared code and present in exactly one source
  set per variant — which is what a **compatibility band** pair is. (It would
  also require ATAK types in `main`.) Note the band dirs are per-BREAK, not
  per-version: both sides of a pair declare the SAME package and class name;
  the directories are build-time selection units, not Java namespaces.

## Consequence: how stable impls avoid 10× duplication

Because no impl may live in `main`, a version-stable impl would otherwise be copied
into all ten flavor source sets. Instead: a **shared impl source set**
(`src/atakShared/java`) holds impls whose ATAK API is source-stable across every
targeted version (written once, added to all flavors); band source sets hold only
divergent impls (identical FQN per band side). Because the divergent classes keep
one FQN, a single **Creator registration point** in `src/atakShared` wires the
right impls for every version without `main` naming an ATAK type or using
reflection. A divergence splits the **compatibility band**; each side gets
its own impl. A build check that fails on any ATAK import in `src/main` enforces the
boundary mechanically.
