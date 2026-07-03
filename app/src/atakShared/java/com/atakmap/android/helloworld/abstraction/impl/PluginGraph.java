package com.atakmap.android.helloworld.abstraction.impl;

import com.atakmap.android.helloworld.abstraction.SystemsCheck;
import com.atakmap.android.helloworld.features.importer.ImportCreator;
import com.atakmap.android.helloworld.features.layerdownload.LayerDownloadCreator;
import com.atakmap.android.helloworld.features.pane.PaneController;
import com.atakmap.android.helloworld.features.radio.RadioCreator;

import javax.inject.Singleton;

import dagger.Component;

/**
 * The plugin's Dagger object graph (ADR-0003). Manually owned — no
 * Android-lifecycle scopes ({@code @Singleton} here just means one instance per
 * graph) — so it bootstraps inside the ATAK plugin classloader where Hilt
 * cannot. Built once at the composition root (the single sanctioned place
 * {@code src/main} names this impl package):
 * {@code DaggerPluginGraph.builder().creatorModule(new CreatorModule(ctx)).build()}.
 * Everything else receives its Creators from here.
 *
 * <p>The interface stays small on purpose: the multibound {@code Set<Creator>}
 * is an implementation detail consumed by {@link SystemsCheck}'s provider in
 * {@link CreatorModule} — callers get the assembled check plus typed accessors,
 * not the set.
 */
@Singleton
@Component(modules = CreatorModule.class)
public interface PluginGraph {

    /** The load-time systems check, assembled over every registered Creator/probe. */
    SystemsCheck systemsCheck();

    RadioCreator radioCreator();

    ImportCreator importCreator();

    LayerDownloadCreator layerDownloadCreator();

    /** The Pane controller — owns the pane's view wiring, dispatches to feature Controllers. */
    PaneController paneController();
}
