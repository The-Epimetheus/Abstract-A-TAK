package com.atakmap.android.helloworld.features.layerdownload;

/**
 * Callback port for layer-download progress: the plugin-owned listener a
 * Controller implements to receive inbound download events. The impl implements
 * ATAK's real {@code LayerDownloader.Callback} and adapts each event to these
 * DTO-typed calls — the worked example of the callback-port pattern.
 */
public interface LayerDownloadPort {

    /** A download could not start (e.g. no mobile imagery loaded for the region). */
    void onUnavailable(String reason);

    /** Tile progress update. */
    void onStatus(LayerDownloadStatus status);

    /** Job phase change. COMPLETE/ERROR/CANCELLED are terminal. */
    void onPhase(LayerDownloadPhase phase);

    /** The overall max-progress value changed. */
    void onMaxProgress(int max);

    /** The current-level max-progress value changed (next level began). */
    void onLevelProgress(int levelMax);
}
