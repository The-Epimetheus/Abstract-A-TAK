package com.atakmap.android.helloworld.features.layerdownload;

import com.atakmap.android.helloworld.abstraction.Handle;

/**
 * Typed {@link Handle} for a live {@code LayerDownloader} the impl retains.
 * {@code src/main} holds this token; the impl keeps the
 * {@code LayerDownloadHandle -> LayerDownloader} registry and evicts on
 * {@link LayerDownloadCreator#dispose}. The type keeps a layer-download handle
 * from being passed where some other feature's handle is expected.
 */
public final class LayerDownloadHandle extends Handle {
}
