package com.atakmap.android.helloworld.features.layers;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's custom map-layer cluster: {@code GLLayerFactory}
 * renderer (SPI) registration, adding/removing layers on the
 * {@code MAP_SURFACE_OVERLAYS} render stack, camera fit-to-layer, and staging
 * the sample image the layers render. The live layer objects
 * ({@code ExampleLayer}, {@code ExampleMultiLayer}, {@code SimpleHeatMapLayer}
 * — still ATAK-typed sample classes in {@code samplelayer/} and
 * {@code heatmap/}, deferred debt) are retained on the impl side of the seam;
 * {@code src/main} drives them through intent-level verbs only and never
 * holds a layer reference.
 *
 * <p>Inbound direction: the overlay-manager custom actions declared in
 * {@code assets/actions/layer_delete.xml} and
 * {@code assets/actions/layer_toggle_visibility.xml} re-enter the plugin as
 * broadcasts on ATAK's internal bus; {@link #registerLayerActions} adapts them
 * onto a plugin-owned {@link LayerActionPort}.
 *
 * <p>Interface in {@code src/main}; implementation in {@code src/atakShared}.
 */
public interface LayerCreator extends Creator {

    /**
     * Extract the plugin's {@code logo.png} asset to
     * {@code tools/helloworld/logo.png} under ATAK's storage root, creating
     * the directory through ATAK's IO provider (so it also works on encrypted
     * IO providers). Idempotent — re-staging overwrites.
     *
     * @return the staged file's absolute path plus whether extraction
     *         succeeded; on failure the path still names the destination so
     *         the caller can report it.
     */
    LayerIconDto stageLayerIcon();

    /**
     * Put the single example layer on the surface render stack (creating it
     * — and lazily registering its GL renderer SPI — on first call), make it
     * visible, pan/zoom the camera to fit it, and refresh Overlay Manager.
     *
     * @param imagePath absolute path of the staged image the layer renders
     *                  (see {@link #stageLayerIcon()}).
     */
    void showExampleLayer(String imagePath);

    /**
     * Remove the single example layer from the surface render stack and
     * refresh Overlay Manager. No-op if it was never shown. Mirrors the
     * legacy demo: the layer object is retained for re-show and its GL SPI
     * stays registered until {@link #disposeLayers()}.
     */
    void hideExampleLayer();

    /**
     * Put the three-altitude example multi-layer stack (50/100/150 m over the
     * fixed demo cell at 50°, -50°) on the surface render stack, creating the
     * trio on first call (or after a hide, which discards them), make each
     * visible, fit the camera to the first, and refresh Overlay Manager.
     */
    void showMultiLayers(String imagePath);

    /**
     * Remove all example multi-layers from the surface render stack, discard
     * them (unlike the single layer they are rebuilt on the next show), and
     * refresh Overlay Manager.
     */
    void hideMultiLayers();

    /**
     * Show the 8x8 simple heat-map layer over the map's current bounds
     * (creating it — and registering its GL SPI — on first call), loading
     * {@code argbData} as its cell colors row-major, then add it to the
     * surface render stack and make it visible. The corners follow the
     * current view bounds on every show.
     */
    void showHeatMap(int[] argbData);

    /** Remove the heat-map layer from the render stack and mark it invisible. */
    void hideHeatMap();

    /**
     * Register {@code port} for the overlay-manager custom layer actions
     * (LAYER_VISIBILITY / LAYER_DELETE broadcasts on ATAK's internal bus,
     * carrying the target meta shape's uid). Re-registering replaces the
     * previous registration.
     */
    void registerLayerActions(LayerActionPort port);

    /** Unregister the {@link #registerLayerActions} receiver. Idempotent. */
    void unregisterLayerActions();

    /**
     * Toggle visibility of the example (single or multi) layer whose meta
     * shape carries {@code uid}; quiet no-op when no such layer is on the
     * surface render stack.
     */
    void toggleLayerVisibility(String uid);

    /**
     * Remove the example (single or multi) layer whose meta shape carries
     * {@code uid} from the surface render stack; quiet no-op when not found.
     */
    void deleteLayer(String uid);

    /**
     * Full teardown for plugin unload: remove every retained layer from the
     * map and unregister the GL renderer SPIs (ATAK hot-reloads plugins;
     * leaked layers and SPIs survive into the next load). Idempotent.
     */
    void disposeLayers();
}
