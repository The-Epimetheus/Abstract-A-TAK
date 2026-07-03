package com.atakmap.android.helloworld.features.layers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.helloworld.heatmap.GLSimpleHeatMapLayer;
import com.atakmap.android.helloworld.heatmap.SimpleHeatMapLayer;
import com.atakmap.android.helloworld.samplelayer.ExampleLayer;
import com.atakmap.android.helloworld.samplelayer.ExampleMultiLayer;
import com.atakmap.android.helloworld.samplelayer.GLExampleLayer;
import com.atakmap.android.helloworld.samplelayer.GLExampleMultiLayer;
import com.atakmap.android.hierarchy.HierarchyListReceiver;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.MapView.RenderStack;
import com.atakmap.android.util.ATAKUtilities;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.io.IOProviderFactory;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.map.layer.Layer;
import com.atakmap.map.layer.opengl.GLLayerFactory;
import com.atakmap.util.zip.IoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The only place the custom map-layer plumbing is touched:
 * {@code GLLayerFactory} SPI registration, the {@code MAP_SURFACE_OVERLAYS}
 * render stack, {@code ATAKUtilities.scaleToFit}, the Overlay Manager
 * refresh broadcast, and the ATAK-IO staging of the sample image. The live
 * layer objects are retained here — {@code src/main} never holds one.
 * Source-stable across all targeted versions → shared impl source set.
 *
 * <p>The sample layer classes themselves ({@code samplelayer/},
 * {@code heatmap/}) still live in {@code src/main} as deferred debt; they are
 * the plugin's own package so the boundary check permits them, and only this
 * impl names them.
 */
public final class LayerCreatorImpl implements LayerCreator {

    private static final String TAG = "LayerCreatorImpl";

    private final Context pluginContext;

    // Live layer state, exactly as the legacy receiver held it: the single
    // layer and the heat map are retained across hide/show; the multi-layer
    // trio is discarded on hide and rebuilt on the next show.
    private ExampleLayer exampleLayer;
    private final Map<Integer, ExampleMultiLayer> exampleMultiLayers = new HashMap<>();
    private SimpleHeatMapLayer simpleHeatMapLayer;

    private BroadcastReceiver layerActionReceiver;

    public LayerCreatorImpl(Context pluginContext) {
        this.pluginContext = pluginContext;
        // Legacy teaching quirk kept on purpose: the multi-layer GL renderer
        // SPI is registered eagerly at load, while the single-layer SPI is
        // registered lazily on first show — both orders work, GLLayerFactory
        // only needs to know the renderer before the layer is added. (The
        // legacy receiver registered this at pane inflation, which also
        // happened once at plugin load.)
        GLLayerFactory.register(GLExampleMultiLayer.SPI);
    }

    @Override
    public String id() {
        return "LayerCreator";
    }

    /**
     * Seam contract: one staging path for both layer demos. The legacy single
     * layer created the directory through {@code IOProviderFactory} while the
     * multi layer used plain {@code File.mkdir()} — an objective bug on
     * encrypted IO providers; both now take the IO-provider-aware path.
     */
    @Override
    public LayerIconDto stageLayerIcon() {
        File f = FileSystemUtils.getItem("tools/helloworld/logo.png");
        File parentFile = f.getParentFile();
        if (parentFile != null && !IOProviderFactory.exists(parentFile)) {
            if (!IOProviderFactory.mkdir(parentFile))
                Log.d(TAG, "could not make the directory");
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            if (FileSystemUtils.assetExists(pluginContext, "logo.png")) {
                FileSystemUtils.copyFromAssets(pluginContext, "logo.png",
                        fos);
            }
        } catch (Exception e) {
            Log.e(TAG, "error exracting: " + f);
            return new LayerIconDto(false, f.getAbsolutePath());
        } finally {
            IoUtils.close(fos);
        }
        return new LayerIconDto(true, f.getAbsolutePath());
    }

    @Override
    public void showExampleLayer(String imagePath) {
        MapView mapView = MapView.getMapView();
        synchronized (this) {
            if (exampleLayer == null) {
                // Register the renderer before the layer ever reaches the map.
                GLLayerFactory.register(GLExampleLayer.SPI);
                exampleLayer = new ExampleLayer(pluginContext,
                        "HelloWorld Test Layer", imagePath);
            }
        }
        // Add the layer to the map
        mapView.addLayer(RenderStack.MAP_SURFACE_OVERLAYS, exampleLayer);
        exampleLayer.setVisible(true);
        // Pan and zoom to the layer
        ATAKUtilities.scaleToFit(mapView, exampleLayer.getPoints(),
                mapView.getWidth(), mapView.getHeight());
        refreshOverlayManager();
    }

    @Override
    public void hideExampleLayer() {
        synchronized (this) {
            if (exampleLayer == null)
                return;
        }
        // Remove the layer from the map (the object is retained for re-show;
        // its SPI stays registered until disposeLayers()).
        MapView.getMapView().removeLayer(RenderStack.MAP_SURFACE_OVERLAYS,
                exampleLayer);
        refreshOverlayManager();
    }

    @Override
    public void showMultiLayers(String imagePath) {
        MapView mapView = MapView.getMapView();
        synchronized (this) {
            if (exampleMultiLayers.isEmpty()) {
                // Three stacked cells over the same footprint, 50m apart —
                // demonstrates several layers coexisting on the render stack.
                for (int index = 0; index < 3; index++) {
                    int altitude = (index + 1) * 50;
                    GeoPoint ul = GeoPoint.createMutable();
                    GeoPoint ur = GeoPoint.createMutable();
                    GeoPoint lr = GeoPoint.createMutable();
                    GeoPoint ll = GeoPoint.createMutable();
                    ur.set(50, -49.999, altitude);
                    lr.set(49.999, -49.999, altitude);
                    ll.set(49.999, -50, altitude);
                    ul.set(50, -50, altitude);
                    ExampleMultiLayer exampleMultiLayer = new ExampleMultiLayer(
                            pluginContext,
                            String.format(
                                    "HelloWorld Test Multi Layer %4d",
                                    altitude),
                            imagePath, ul, ur, lr, ll);
                    exampleMultiLayers.put(altitude, exampleMultiLayer);
                }
            }
        }
        if (!exampleMultiLayers.isEmpty()) {
            for (ExampleMultiLayer layer : exampleMultiLayers.values()) {
                // Add the layer to the map
                mapView.addLayer(RenderStack.MAP_SURFACE_OVERLAYS, layer);
                layer.setVisible(true);
            }
        }
        // Pan and zoom to the layer
        ATAKUtilities.scaleToFit(mapView,
                exampleMultiLayers.entrySet().iterator().next()
                        .getValue().getPoints(),
                mapView.getWidth(), mapView.getHeight());
        refreshOverlayManager();
    }

    @Override
    public void hideMultiLayers() {
        MapView mapView = MapView.getMapView();
        synchronized (this) {
            if (!exampleMultiLayers.isEmpty()) {
                for (ExampleMultiLayer layer : exampleMultiLayers.values()) {
                    // Remove the layer from the map
                    mapView.removeLayer(RenderStack.MAP_SURFACE_OVERLAYS,
                            layer);
                }
                exampleMultiLayers.clear();
            }
        }
        refreshOverlayManager();
    }

    @Override
    public void showHeatMap(int[] argbData) {
        MapView mapView = MapView.getMapView();
        if (simpleHeatMapLayer == null) {
            GLLayerFactory.register(GLSimpleHeatMapLayer.SPI);
            simpleHeatMapLayer = new SimpleHeatMapLayer(pluginContext,
                    "simple heat map",
                    8, 8, mapView.getBounds());
        }
        // The corners track the CURRENT view bounds on every show, not the
        // bounds the layer was created with.
        simpleHeatMapLayer.setCorners(mapView.getBounds());
        simpleHeatMapLayer.setData(argbData);
        simpleHeatMapLayer.refresh();
        mapView.addLayer(RenderStack.MAP_SURFACE_OVERLAYS,
                simpleHeatMapLayer);
        simpleHeatMapLayer.setVisible(true);
    }

    @Override
    public void hideHeatMap() {
        if (simpleHeatMapLayer == null)
            return;
        MapView.getMapView().removeLayer(RenderStack.MAP_SURFACE_OVERLAYS,
                simpleHeatMapLayer);
        simpleHeatMapLayer.setVisible(false);
    }

    @Override
    public void registerLayerActions(final LayerActionPort port) {
        unregisterLayerActions();
        layerActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action == null)
                    return;
                // Both action XMLs put the target meta shape's uid on the
                // intent — that is the whole contract with the port.
                final String uid = intent.getStringExtra("uid");
                if (LayersController.LAYER_VISIBILITY.equals(action))
                    port.onToggleVisibility(uid);
                else if (LayersController.LAYER_DELETE.equals(action))
                    port.onDeleteLayer(uid);
            }
        };
        AtakBroadcast.DocumentedIntentFilter filter = new AtakBroadcast.DocumentedIntentFilter();
        filter.addAction(LayersController.LAYER_DELETE,
                "Delete example layer");
        filter.addAction(LayersController.LAYER_VISIBILITY,
                "Toggle visibility of example layer");
        AtakBroadcast.getInstance().registerReceiver(layerActionReceiver,
                filter);
    }

    @Override
    public void unregisterLayerActions() {
        if (layerActionReceiver == null)
            return;
        AtakBroadcast.getInstance().unregisterReceiver(layerActionReceiver);
        layerActionReceiver = null;
    }

    @Override
    public void toggleLayerVisibility(String uid) {
        ExampleLayer l = findLayer(uid);
        if (l != null) {
            l.setVisible(!l.isVisible());
        } else {
            ExampleMultiLayer ml = findMultiLayer(uid);
            if (ml != null)
                ml.setVisible(!ml.isVisible());
        }
    }

    @Override
    public void deleteLayer(String uid) {
        MapView mapView = MapView.getMapView();
        ExampleLayer l = findLayer(uid);
        if (l != null) {
            mapView.removeLayer(RenderStack.MAP_SURFACE_OVERLAYS, l);
        } else {
            ExampleMultiLayer ml = findMultiLayer(uid);
            if (ml != null)
                mapView.removeLayer(RenderStack.MAP_SURFACE_OVERLAYS, ml);
        }
    }

    @Override
    public void disposeLayers() {
        MapView mapView = MapView.getMapView();
        try {
            if (exampleLayer != null && mapView != null) {
                mapView.removeLayer(RenderStack.MAP_SURFACE_OVERLAYS,
                        exampleLayer);
                GLLayerFactory.unregister(GLExampleLayer.SPI);
            }
            exampleLayer = null;
        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
        // Legacy bugs fixed inside the seam (with the Disposable contract's
        // license): the legacy disposeImpl() left the multi layers and the
        // heat map on the map — and the heat-map SPI registered — across hot
        // reloads. Tear them down like the single layer.
        try {
            if (mapView != null) {
                for (ExampleMultiLayer layer : exampleMultiLayers.values())
                    mapView.removeLayer(RenderStack.MAP_SURFACE_OVERLAYS,
                            layer);
            }
            exampleMultiLayers.clear();
        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
        GLLayerFactory.unregister(GLExampleMultiLayer.SPI);
        try {
            if (simpleHeatMapLayer != null) {
                if (mapView != null)
                    mapView.removeLayer(RenderStack.MAP_SURFACE_OVERLAYS,
                            simpleHeatMapLayer);
                GLLayerFactory.unregister(GLSimpleHeatMapLayer.SPI);
            }
            simpleHeatMapLayer = null;
        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
    }

    /* --------------------------------- helpers ---------------------------------- */

    /** Refresh Overlay Manager so the layer list reflects the change. */
    private void refreshOverlayManager() {
        AtakBroadcast.getInstance().sendBroadcast(new Intent(
                HierarchyListReceiver.REFRESH_HIERARCHY));
    }

    /**
     * Scan the surface render stack for the example layer whose meta shape
     * carries {@code uid}. Same walk as {@code HelloWorldMapOverlay.findLayer}
     * minus its re-parenting side effect — the custom action can only fire
     * from a shape already listed under the overlay, so the shape has a group.
     */
    private ExampleLayer findLayer(String uid) {
        List<Layer> layers = MapView.getMapView().getLayers(
                RenderStack.MAP_SURFACE_OVERLAYS);
        for (Layer l : layers) {
            if (l instanceof ExampleLayer
                    && ((ExampleLayer) l).getMetaShape().getUID()
                            .equals(uid))
                return (ExampleLayer) l;
        }
        return null;
    }

    private ExampleMultiLayer findMultiLayer(String uid) {
        List<Layer> layers = MapView.getMapView().getLayers(
                RenderStack.MAP_SURFACE_OVERLAYS);
        for (Layer l : layers) {
            if (l instanceof ExampleMultiLayer
                    && ((ExampleMultiLayer) l).getMetaShape().getUID()
                            .equals(uid))
                return (ExampleMultiLayer) l;
        }
        return null;
    }

    /**
     * PARTIAL by design: adding a real layer would flash user-visibly on the
     * map, so the probe exercises the GL SPI registration path (register +
     * unregister in a finally — the demo registers this SPI lazily, so
     * load-time state is unchanged) and queries the real render stack.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "layer plumbing threw", () -> {
            MapView mv = MapView.getMapView();
            if (mv == null)
                return SelfCheckResult.skipped(id(), "MapView not ready");
            synchronized (this) {
                if (exampleLayer != null)
                    // The demo layer exists, so the whole real path already
                    // ran — don't cycle the SPI out from under it.
                    return SelfCheckResult.partial(id(),
                            "example layer live — real path already exercised by the demo");
                GLLayerFactory.register(GLExampleLayer.SPI);
                try {
                    List<Layer> layers = mv.getLayers(
                            RenderStack.MAP_SURFACE_OVERLAYS);
                    return SelfCheckResult.partial(id(),
                            "GL SPI registered+unregistered; surface render stack queried ("
                                    + layers.size()
                                    + " layers); no layer added (would be user-visible)");
                } finally {
                    GLLayerFactory.unregister(GLExampleLayer.SPI);
                }
            }
        });
    }
}
