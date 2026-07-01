package com.atakmap.android.helloworld.abstraction.impl;

import com.atakmap.android.helloworld.abstraction.Creator;
import com.atakmap.android.helloworld.abstraction.ShellProbe;
import com.atakmap.android.helloworld.features.cot.CotCreatorImpl;
import com.atakmap.android.helloworld.features.radio.RadioCreatorImpl;

import java.util.Collections;
import java.util.Set;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;

/**
 * Dagger module contributing each compiled-in Creator into a {@code Set<Creator>} via
 * {@code @IntoSet} multibindings (ADR-0003). New Creators are added with one
 * {@code @Provides @IntoSet} line — the systems check picks them up automatically.
 *
 * <p>Lives in {@code src/atakShared} because it names the concrete impls (which touch
 * ATAK). {@code src/main} stays ATAK-free.
 */
@Module
public class CreatorModule {

    @Provides
    @IntoSet
    Creator provideCotCreator() {
        return new CotCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator provideRadioCreator() {
        return new RadioCreatorImpl();
    }

    @Provides
    @ElementsIntoSet
    Set<ShellProbe> provideShellProbes() {
        // No Humble-shell probes registered yet; returns an empty contribution.
        return Collections.emptySet();
    }
}
