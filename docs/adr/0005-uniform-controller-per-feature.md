# Every feature gets a Controller, even behaviour-free ones

As the receiver decomposition proceeds (Humble shell → pane controller → feature
Controllers), each migrated feature gets a **Controller** — including features
whose Controller is a bare pass-through to its **Creator** and therefore a
shallow module by the deletion test. Chosen because this repo is a teaching
reference: a reader copying the pattern should see exactly one shape
(tap → pane controller → Controller → Creator), not a per-feature judgment call
about whether a Controller is warranted.

## Considered options

- **Controller only where behaviour exists** (behaviour-free taps call their
  Creator straight from the pane wiring): fewer shallow modules, but readers
  see two shapes and must re-derive the rule for each new feature. Rejected as
  the less teachable shape.
- **Uniform Controller per feature (chosen).**

## Consequences

- Some Controllers are deliberately shallow. Architecture reviews should not
  flag them for deletion — the same license ADR-0002 grants **insurance seams**
  applies here: uniformity, not leverage, is what these modules buy.
- Behaviour later added to a feature already has its home; no feature ever
  graduates from "wiring calls Creator" to "now we need a Controller".
