package com.atakmap.android.helloworld.features.radio;

import android.view.View;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's radio control panel ({@code RadioMapComponent}). Registering
 * a control is version-sensitive: the {@code registerControl(View)} overload was removed
 * in ATAK 5.1 in favor of a keyed {@code registerControl(String, View)} (present in every
 * version). Business logic depends only on this interface and passes a plugin key + an
 * Android {@link View} — never touching {@code RadioMapComponent}.
 *
 * <p>Interface in {@code src/main} (ATAK-free — {@code View} is Android, not ATAK); the
 * implementation lives in {@code src/atakShared} because the keyed overload is stable
 * across all supported versions.
 */
public interface RadioCreator extends Creator {

    /** Register a control view under a plugin-owned key. */
    void registerControl(String key, View control);

    /** Unregister the control previously registered under {@code key}. */
    void unregisterControl(String key);
}
