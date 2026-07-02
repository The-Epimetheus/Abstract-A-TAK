package com.atakmap.android.helloworld.abstraction.impl;

import android.content.Context;

import com.atakmap.android.helloworld.abstraction.Creator;
import com.atakmap.android.helloworld.abstraction.ShellProbe;
import com.atakmap.android.helloworld.abstraction.SystemsCheck;
import com.atakmap.android.helloworld.features.cot.CotCreatorImpl;
import com.atakmap.android.helloworld.features.importer.ImportCreator;
import com.atakmap.android.helloworld.features.importer.ImportCreatorImpl;
import com.atakmap.android.helloworld.features.layerdownload.LayerDownloadCreator;
import com.atakmap.android.helloworld.features.layerdownload.LayerDownloadCreatorImpl;
import com.atakmap.android.helloworld.features.location.LocationCreator;
import com.atakmap.android.helloworld.features.location.LocationCreatorImpl;
import com.atakmap.android.helloworld.features.menu.MenuCreator;
import com.atakmap.android.helloworld.features.menu.MenuCreatorImpl;
import com.atakmap.android.helloworld.features.radio.RadioCreator;
import com.atakmap.android.helloworld.features.radio.RadioCreatorImpl;
import com.atakmap.android.helloworld.features.route.RouteController;
import com.atakmap.android.helloworld.features.route.RouteCreator;
import com.atakmap.android.helloworld.features.route.RouteCreatorImpl;
import com.atakmap.android.helloworld.features.video.VideoCreator;
import com.atakmap.android.helloworld.features.video.VideoCreatorImpl;
import com.atakmap.android.helloworld.navstack.NavigationStackShellProbe;

import java.util.Collections;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;

/**
 * The ONE registration point for Creator implementations (ADR-0003). Each
 * compiled-in Creator is contributed into a {@code Set<Creator>} via
 * {@code @IntoSet} multibindings; the systems check walks that set. Adding a
 * Creator means one typed {@code @Provides} + one {@code @IntoSet} delegation
 * here — nothing else.
 *
 * <p>Typed bindings are {@code @Singleton} so the instance the systems check
 * exercises is the same one handed to consumers (Creators that hold state —
 * a registered importer, a menu factory, a downloader registry — need that).
 *
 * <p>Constructed with the plugin {@link Context} at the composition root, which
 * a few impls need (menu factory inflation, import cache dir). Lives in
 * {@code src/atakShared} because it names the concrete impls (which touch
 * ATAK). {@code src/main} stays ATAK-free. Don't want Dagger in your own
 * plugin? The hand-wired equivalent of this module is a dozen lines — see
 * MIGRATION.md, "Without Dagger".
 */
@Module
public class CreatorModule {

    private final Context pluginContext;

    public CreatorModule(Context pluginContext) {
        this.pluginContext = pluginContext;
    }

    // ---- CotCreator: exercised by the systems check only (no typed consumer yet)

    @Provides
    @IntoSet
    Creator provideCotCreator() {
        return new CotCreatorImpl();
    }

    // ---- Creators src/main consumes directly: typed binding + set delegation

    @Provides
    @Singleton
    RadioCreator provideRadioCreator() {
        return new RadioCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator radioCreatorIntoSet(RadioCreator c) {
        return c;
    }

    @Provides
    @Singleton
    LocationCreator provideLocationCreator() {
        return new LocationCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator locationCreatorIntoSet(LocationCreator c) {
        return c;
    }

    @Provides
    @Singleton
    VideoCreator provideVideoCreator() {
        return new VideoCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator videoCreatorIntoSet(VideoCreator c) {
        return c;
    }

    @Provides
    @Singleton
    ImportCreator provideImportCreator() {
        return new ImportCreatorImpl(pluginContext);
    }

    @Provides
    @IntoSet
    Creator importCreatorIntoSet(ImportCreator c) {
        return c;
    }

    @Provides
    @Singleton
    MenuCreator provideMenuCreator() {
        return new MenuCreatorImpl(pluginContext);
    }

    @Provides
    @IntoSet
    Creator menuCreatorIntoSet(MenuCreator c) {
        return c;
    }

    @Provides
    @Singleton
    LayerDownloadCreator provideLayerDownloadCreator() {
        return new LayerDownloadCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator layerDownloadCreatorIntoSet(LayerDownloadCreator c) {
        return c;
    }

    @Provides
    @Singleton
    RouteCreator provideRouteCreator() {
        return new RouteCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator routeCreatorIntoSet(RouteCreator c) {
        return c;
    }

    // Controllers are wired here too: constructor-injected with their Creators
    // (ADR-0003), they stay ATAK-free and testable through their interface.
    @Provides
    @Singleton
    RouteController provideRouteController(RouteCreator routeCreator) {
        return new RouteController(routeCreator, pluginContext);
    }

    // ---- Humble-shell probes + the assembled systems check

    @Provides
    @ElementsIntoSet
    Set<ShellProbe> provideShellProbes() {
        // One probe per LAZILY-created Humble shell (eager shells fail loudly at
        // load on their own): construct + dispose the real shell so a base-type
        // break surfaces now, not at first open.
        return Collections.singleton(
                new NavigationStackShellProbe(pluginContext));
    }

    @Provides
    SystemsCheck provideSystemsCheck(Set<Creator> creators,
            Set<ShellProbe> shellProbes) {
        return new SystemsCheck(creators, shellProbes);
    }
}
