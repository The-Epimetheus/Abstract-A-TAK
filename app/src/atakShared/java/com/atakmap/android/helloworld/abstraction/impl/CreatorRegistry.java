package com.atakmap.android.helloworld.abstraction.impl;

import com.atakmap.android.helloworld.abstraction.Creator;
import com.atakmap.android.helloworld.abstraction.ShellProbe;
import com.atakmap.android.helloworld.abstraction.SystemsCheck;
import com.atakmap.android.helloworld.features.cot.CotCreatorImpl;
import com.atakmap.android.helloworld.features.radio.RadioCreator;
import com.atakmap.android.helloworld.features.radio.RadioCreatorImpl;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The seam through which {@code src/main} obtains Creators and the systems check
 * without naming an ATAK type. Lives in {@code src/atakShared} because it names the
 * concrete impls (which touch ATAK).
 *
 * <p>Wiring: {@link #buildSystemsCheck()} uses the <b>Dagger graph</b>
 * ({@link CreatorModule} contributes each Creator via {@code @IntoSet};
 * {@link PluginGraph} exposes the sets) — adding a Creator means one
 * {@code @Provides @IntoSet} line there. The manual {@link #creators()} /
 * {@link #shellProbes()} methods below duplicate that wiring <b>on purpose</b>: they
 * are the dependency-free version of the same pattern, kept as a reference for
 * plugin authors who copy this project but don't want Dagger — delete them (or the
 * Dagger pieces) in your own plugin; keep one mechanism, not both.
 */
public final class CreatorRegistry {

    private CreatorRegistry() {}

    public static Set<Creator> creators() {
        Set<Creator> creators = new LinkedHashSet<>();
        creators.add(new CotCreatorImpl());
        creators.add(new RadioCreatorImpl());
        // ... remaining Creators added here as each feature is migrated.
        return creators;
    }

    /** Typed accessor for src/main to obtain a RadioCreator without naming an ATAK type. */
    public static RadioCreator radioCreator() {
        return new RadioCreatorImpl();
    }

    public static Set<ShellProbe> shellProbes() {
        Set<ShellProbe> probes = new LinkedHashSet<>();
        // ... Humble-shell probes added here as shells are migrated.
        return probes;
    }

    public static SystemsCheck buildSystemsCheck() {
        // Dagger multibindings (@IntoSet) supply the Creator/ShellProbe sets — adding a
        // Creator is one @Provides line in CreatorModule. The manual creators()/
        // shellProbes() above remain as a dependency-free fallback/reference.
        PluginGraph graph = DaggerPluginGraph.create();
        return new SystemsCheck(graph.creators(), graph.shellProbes());
    }
}
