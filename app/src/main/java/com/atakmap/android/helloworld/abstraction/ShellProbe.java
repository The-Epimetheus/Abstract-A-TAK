package com.atakmap.android.helloworld.abstraction;

/**
 * The load-time verification contract for a Humble shell — a plugin class that must
 * extend an ATAK base type ({@code DropDownReceiver}, {@code MapComponent}, ...).
 *
 * <p>A shell carries version-risk the Creator seam does not: if its ATAK base type's
 * constructor/signature changes, the shell breaks. Entry-point shells fail loudly via
 * ATAK's own loader, but a lazily-created shell would otherwise only fail when the
 * user first opens it. So each shell provides a ShellProbe that the load-time systems
 * check runs: it constructs the real shell and disposes it, surfacing a base-class
 * break at load.
 *
 * <p>The interface is ATAK-free and lives in {@code src/main}; implementations (which
 * construct the real ATAK subclass) live in the impl source sets. Probes are collected
 * into a {@code Set<ShellProbe>} via Dagger, alongside the {@code Set<Creator>}.
 */
public interface ShellProbe {

    /** Identifier for log lines (e.g. {@code "HelloWorldDropDownShell"}). */
    String id();

    /**
     * Best-effort construct-and-dispose of the shell. Must never throw — catch and
     * return {@link SelfCheckResult#failed}. Dispose in a {@code finally}.
     */
    SelfCheckResult probe();
}
