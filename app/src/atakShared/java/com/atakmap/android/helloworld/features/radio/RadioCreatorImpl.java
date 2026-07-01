package com.atakmap.android.helloworld.features.radio;

import android.view.View;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.radiolibrary.RadioMapComponent;

/**
 * The only place {@code RadioMapComponent} is touched. Uses the keyed
 * {@code registerControl(String, View)} / {@code unregisterControl(String)} overloads,
 * which are byte-confirmed present in every supported ATAK version (the older
 * {@code registerControl(View)} was removed in 5.1) — so one impl serves all versions
 * and it lives in {@code src/atakShared}.
 */
public final class RadioCreatorImpl implements RadioCreator {

    private static final String TEST_KEY = "HW-SELFCHECK-radio";

    @Override
    public String id() {
        return "RadioCreator";
    }

    @Override
    public void registerControl(String key, View control) {
        RadioMapComponent.getInstance().registerControl(key, control);
    }

    @Override
    public void unregisterControl(String key) {
        RadioMapComponent.getInstance().unregisterControl(key);
    }

    /**
     * Real op + teardown: register a throwaway control under the reserved test key and
     * unregister it in a {@code finally}. Skips gracefully if the radio subsystem or
     * MapView is not ready at load.
     */
    @Override
    public SelfCheckResult selfCheck() {
        View probe = null;
        try {
            MapView mv = MapView.getMapView();
            if (mv == null || RadioMapComponent.getInstance() == null) {
                return SelfCheckResult.skipped(id(), "MapView/RadioMapComponent not ready");
            }
            probe = new View(mv.getContext());
            RadioMapComponent.getInstance().registerControl(TEST_KEY, probe);
            return SelfCheckResult.full(id(), "registered + unregistered a test control");
        } catch (Throwable t) {
            return SelfCheckResult.failed(id(), "radio control path threw", t);
        } finally {
            try {
                RadioMapComponent.getInstance().unregisterControl(TEST_KEY);
            } catch (Throwable ignore) {
                // best-effort teardown
            }
        }
    }
}
