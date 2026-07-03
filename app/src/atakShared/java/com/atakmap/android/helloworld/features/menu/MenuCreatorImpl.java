package com.atakmap.android.helloworld.features.menu;

import android.content.Context;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.menu.MenuFactory;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.menu.MapMenuReceiver;
import com.atakmap.android.menu.PluginMenuParser;

/**
 * The only place {@code MapMenuReceiver} factory registration is touched. The
 * factory itself — {@link MenuFactory}, whose widget API moved to
 * {@code gov.tak.api.widgets} in 5.5 — is whole-class banded
 * (src/bands/atakPre55 / src/bands/atak55plus); this impl owns the single instance and
 * stays in {@code src/atakShared} (same FQN on both band sides, so it compiles
 * against exactly one).
 */
public final class MenuCreatorImpl implements MenuCreator {

    private final Context pluginContext;
    private MenuFactory factory;

    public MenuCreatorImpl(Context pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public String id() {
        return "MenuCreator";
    }

    @Override
    public void enableCustomRadialMenus() {
        if (factory == null)
            factory = new MenuFactory(pluginContext);
        MapMenuReceiver.getInstance().registerMapMenuFactory(factory);
    }

    @Override
    public void disableCustomRadialMenus() {
        if (factory != null)
            MapMenuReceiver.getInstance().unregisterMapMenuFactory(factory);
    }

    @Override
    public String loadRadialMenu(String assetName) {
        return PluginMenuParser.getMenu(pluginContext, assetName);
    }

    @Override
    public void overrideMenuForType(String type, String sourceType) {
        // Previously this was done by intent and we were unable to get the
        // menu based on a specific type; MapMenuReceiver's direct
        // registerMenu/lookupMenu pair is what makes it possible.
        MapMenuReceiver.getInstance().registerMenu(type,
                MapMenuReceiver.getInstance().lookupMenu(sourceType));
    }

    @Override
    public void clearMenuOverrideForType(String type) {
        // Previously this was done by intent.
        MapMenuReceiver.getInstance().unregisterMenu(type);
    }

    /**
     * Constructs a throwaway banded {@link MenuFactory} and registers +
     * unregisters it (invisible and reversible — no menu is opened). PARTIAL
     * because widget production only runs when a radial menu actually opens.
     */
    @Override
    public SelfCheckResult selfCheck() {
        MenuFactory probe = null;
        try {
            if (MapView.getMapView() == null
                    || MapMenuReceiver.getInstance() == null)
                return SelfCheckResult.skipped(id(),
                        "MapView/MapMenuReceiver not ready");
            probe = new MenuFactory(pluginContext);
            MapMenuReceiver.getInstance().registerMapMenuFactory(probe);
            return SelfCheckResult.partial(id(),
                    "banded factory constructed + registered + unregistered; "
                            + "widget production needs an open radial menu");
        } catch (Throwable t) {
            return SelfCheckResult.failed(id(), "banded menu factory path threw", t);
        } finally {
            try {
                if (probe != null)
                    MapMenuReceiver.getInstance().unregisterMapMenuFactory(probe);
            } catch (Throwable ignore) {
                // best-effort teardown
            }
        }
    }
}
