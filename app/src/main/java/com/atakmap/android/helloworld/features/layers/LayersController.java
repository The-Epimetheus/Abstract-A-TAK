package com.atakmap.android.helloworld.features.layers;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.atakmap.android.helloworld.abstraction.Disposable;

/**
 * The layers feature's Controller: the three layer demos (single example
 * layer, three-altitude multi-layer, simple heat map) plus the
 * overlay-manager custom actions, all ATAK-free — the GL SPI registration,
 * render-stack calls and live layer objects live behind {@link LayerCreator}.
 *
 * <p>Holds live map layers through the Creator, so it implements
 * {@link Disposable}; the Pane controller cascades {@link #dispose()} on
 * plugin unload.
 */
public class LayersController implements LayerActionPort, Disposable {

    private static final String TAG = "LayersController";

    /**
     * Broadcasts the overlay-manager custom actions
     * ({@code assets/actions/layer_delete.xml} /
     * {@code layer_toggle_visibility.xml}) re-enter the plugin on;
     * {@link LayerCreator#registerLayerActions} listens for them and delivers
     * to this Controller's {@link LayerActionPort}.
     */
    public static final String LAYER_DELETE = "com.atakmap.android.helloworld.LAYER_DELETE";
    public static final String LAYER_VISIBILITY = "com.atakmap.android.helloworld.LAYER_VISIBILITY";

    private final LayerCreator layerCreator;
    private final Context pluginContext;

    public LayersController(LayerCreator layerCreator, Context pluginContext) {
        this.layerCreator = layerCreator;
        this.pluginContext = pluginContext;
    }

    /**
     * Toggle the single example layer on/off the map, staging its image from
     * plugin assets first (both directions stage, mirroring the legacy demo).
     *
     * @param currentlyShown the button's selected state before the tap.
     * @return true when the toggle happened (the caller flips the button's
     *         selected state); false when the image could not be staged (a
     *         toast was already shown and nothing changed).
     */
    public boolean toggleExampleLayer(boolean currentlyShown) {
        LayerIconDto icon = layerCreator.stageLayerIcon();
        if (!icon.staged()) {
            toast("file: " + icon.path()
                    + " does not exist, please create it before trying this example");
            return false;
        }
        if (currentlyShown)
            layerCreator.hideExampleLayer();
        else
            layerCreator.showExampleLayer(icon.path());
        return true;
    }

    /**
     * Toggle the three-altitude example multi-layer stack on/off the map;
     * same staging + return contract as {@link #toggleExampleLayer}.
     */
    public boolean toggleMultiLayers(boolean currentlyShown) {
        LayerIconDto icon = layerCreator.stageLayerIcon();
        if (!icon.staged()) {
            toast("file: " + icon.path()
                    + " does not exist, please create it before trying this example");
            return false;
        }
        if (currentlyShown)
            layerCreator.hideMultiLayers();
        else
            layerCreator.showMultiLayers(icon.path());
        return true;
    }

    /**
     * Show or hide the simple heat map. Unlike the other two demos this
     * follows the button's state AFTER the tap flips it (legacy quirk), and
     * regenerates the demo data on every show.
     */
    public void toggleHeatMap(boolean showNow) {
        if (showNow)
            layerCreator.showHeatMap(generateHeatMap());
        else
            layerCreator.hideHeatMap();
    }

    /* ---------- LayerActionPort: overlay-manager custom actions ---------- */

    @Override
    public void onToggleVisibility(String uid) {
        Log.d(TAG,
                "used the custom action to toggle layer visibility on: "
                        + uid);
        layerCreator.toggleLayerVisibility(uid);
    }

    @Override
    public void onDeleteLayer(String uid) {
        Log.d(TAG,
                "used the custom action to delete the layer on: "
                        + uid);
        layerCreator.deleteLayer(uid);
    }

    /* --------------------------------- lifecycle -------------------------------- */

    /**
     * Unregister the custom-action listener and tear every retained layer off
     * the map (idempotent — the Creator guards re-entry). Cascaded by
     * PaneController on plugin unload.
     */
    @Override
    public void dispose() {
        layerCreator.unregisterLayerActions();
        layerCreator.disposeLayers();
    }

    /* --------------------------------- helpers ---------------------------------- */

    /**
     * The demo heat-map cells: an 8x8 intensity grid turned into ARGB values.
     * Note - this will become a API offering in 4.5.1 and beyond.
     */
    private int[] generateHeatMap() {
        int[] data = new int[] {
                1, 1, 2, 0, 0, 0, 0, 0,
                1, 4, 2, 0, 0, 0, 0, 0,
                2, 2, 0, 0, 0, 0, 0, 0,
                6, 6, 0, 3, 0, 0, 0, 3,
                6, 6, 5, 3, 1, 3, 3, 3,
                6, 6, 3, 3, 0, 3, 6, 6,
                3, 3, 3, 0, 0, 0, 6, 5,
                3, 0, 0, 0, 0, 0, 5, 5,
        };
        for (int i = 0; i < data.length; ++i) {
            if (data[i] != 0)
                data[i] = Color.BLACK / data[i];
        }
        return data;

    }

    private void toast(String msg) {
        Toast.makeText(pluginContext, msg, Toast.LENGTH_LONG).show();
    }
}
