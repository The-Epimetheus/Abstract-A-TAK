package com.atakmap.android.helloworld.features.layerdownload;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's map-tile layer downloader. Version-sensitive twice
 * over, both absorbed by the atakPre55/atak55plus band behind this seam:
 * {@code LayerDownloader(MapView)} became {@code LayerDownloader(Context)} and
 * the per-setter configuration + {@code startDownload()} became a
 * {@code RequestBuilder} + {@code startDownload(rb)} in ATAK 5.5. (The imagery
 * query underneath, {@code RasterUtils.queryDatasets}, is the version-stable
 * form of an API that already broke once at 5.1.)
 *
 * <p>Interface in {@code src/main}: the region shape is referenced by its map
 * UID, progress flows back through {@link LayerDownloadPort} as Plugin DTOs,
 * and the live downloader the impl retains is held as a
 * {@link LayerDownloadHandle} — never the ATAK object.
 */
public interface LayerDownloadCreator extends Creator {

    /**
     * Resolve the currently-loaded mobile imagery for the region shape
     * {@code shapeUid} (expanded by {@code expandDistanceMeters} for routes),
     * then create and start a tile download named {@code title}. If the shape
     * was a temporary selection artifact it is cleaned off the map. Progress is
     * delivered on {@code port}.
     *
     * @return a handle to the live download, or {@code null} if it could not
     *         start ({@code port.onUnavailable} explains why).
     */
    LayerDownloadHandle startDownload(String title, String shapeUid,
            double expandDistanceMeters, LayerDownloadPort port);

    /** Cancel the download behind {@code handle} (no-op if already finished). */
    void stopDownload(LayerDownloadHandle handle);

    /** Release the downloader behind {@code handle} and evict it from the registry. */
    void dispose(LayerDownloadHandle handle);
}
