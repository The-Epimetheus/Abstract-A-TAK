package com.atakmap.android.helloworld.abstraction;

/**
 * Base type for every Creator — an abstraction over one cluster of version-sensitive
 * ATAK interaction (CoT construction, markers, layers, ...). Business logic
 * (Controllers) depends only on Creator interfaces, never on an ATAK type.
 *
 * <p>Concrete implementations live OUTSIDE {@code src/main}: in
 * {@code src/atakShared/java} when their ATAK API is stable across every supported
 * version, or in a per-version {@code src/atak<NNN>/java} source set when it diverges.
 * Exactly one implementation is compiled into each APK; the compiled-in Creator
 * factory wires it. There is no runtime version switch and no reflection.
 *
 * <p>Every Creator is collected (via Dagger {@code @IntoSet}) into a
 * {@code Set<Creator>} that the load-time systems check walks, invoking
 * {@link #selfCheck()} on each.
 */
public interface Creator {

    /**
     * Stable identifier for this Creator, used in systems-check log lines
     * (e.g. {@code "CotCreator"}). Must be unique across the Set.
     */
    String id();

    /**
     * Best-effort load-time self test. Performs this Creator's real ATAK operation
     * and tears it down (reserved test-artifact namespace, teardown in a
     * {@code finally}); degrades to {@link VerificationLevel#PARTIAL}/
     * {@link VerificationLevel#SKIPPED} where the effect is irreversible/external,
     * rather than leaking. Must never throw — catch and return
     * {@link SelfCheckResult#failed}.
     */
    SelfCheckResult selfCheck();
}
