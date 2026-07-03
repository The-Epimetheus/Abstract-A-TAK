package com.atakmap.android.helloworld.features.layers;

/**
 * Callback port for the overlay-manager custom layer actions. The action XML
 * under {@code assets/actions/} broadcasts
 * {@link LayersController#LAYER_VISIBILITY} / {@link LayersController#LAYER_DELETE}
 * on ATAK's internal bus with the target layer's meta-shape uid;
 * {@link LayerCreator#registerLayerActions} adapts intent → uid → this port.
 * Plugin-owned and ATAK-free; {@link LayersController} implements it.
 */
public interface LayerActionPort {

    /** The user tapped the custom "toggle visibility" action on a layer. */
    void onToggleVisibility(String uid);

    /** The user tapped the custom "delete" action on a layer. */
    void onDeleteLayer(String uid);
}
