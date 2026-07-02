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
- **Default impl in `main`, override per-flavor** (Gradle's native
  flavor-overrides-`main`): rejected — it requires ATAK types in `main`.

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
