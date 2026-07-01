package com.atakmap.android.helloworld.abstraction.impl;

import com.atakmap.android.helloworld.abstraction.Creator;
import com.atakmap.android.helloworld.abstraction.ShellProbe;

import java.util.Set;

import dagger.Component;

/**
 * The plugin's Dagger object graph (ADR-0003). Manually owned — no Android-lifecycle
 * scopes — so it bootstraps inside the ATAK plugin classloader where Hilt cannot.
 * Exposes the multibound {@code Set<Creator>} + {@code Set<ShellProbe>} the load-time
 * systems check walks. Dagger generates {@code DaggerPluginGraph} from this.
 */
@Component(modules = CreatorModule.class)
public interface PluginGraph {

    Set<Creator> creators();

    Set<ShellProbe> shellProbes();
}
