# Verification is against real ATAK, not fakes

Controller/plugin behavior is verified with **instrumented tests on-device** plus
the **load-time systems check**, not JVM unit tests with fake Creators. The
abstraction's purpose here is clean version-isolation and maintenance — not enabling
fast fake-based unit tests.

## Why

The failure mode this project exists to catch is a changed or removed ATAK API on a
given host (`NoSuchMethodError`, behavior drift). A fake `CotCreator` cannot detect
that — only exercising the real per-version impl against real ATAK can. So the
meaningful signal comes from instrumented tests and from the **selfCheck** sweep the
systems check runs at every load (a real-ATAK smoke test, the fast-ish feedback
loop).

## Consequences

- Controllers need not stay "android-light" for unit-testability reasons.
- Meaningful CI requires an emulator/device per ATAK version; the systems check
  partially fills the fast-feedback gap between full instrumented runs.
- A reader expecting JUnit tests on the decoupled Controllers will find none by
  design — this ADR is why.
