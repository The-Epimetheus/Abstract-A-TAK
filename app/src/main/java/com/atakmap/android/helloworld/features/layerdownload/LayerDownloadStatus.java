package com.atakmap.android.helloworld.features.layerdownload;

/**
 * Plugin DTO carrying one tile-download progress update across the seam
 * (mirrors the data of ATAK's {@code LayerDownloader.DownloadStatus} without
 * naming it).
 */
public final class LayerDownloadStatus {

    public final String title;
    public final String tileStatus;
    public final String layerStatus;
    public final int tilesDownloaded;
    public final int totalTiles;
    public final int levelTotalTiles;
    /** Estimated time remaining, milliseconds. */
    public final long timeLeftMillis;

    public LayerDownloadStatus(String title, String tileStatus, String layerStatus,
            int tilesDownloaded, int totalTiles, int levelTotalTiles,
            long timeLeftMillis) {
        this.title = title;
        this.tileStatus = tileStatus;
        this.layerStatus = layerStatus;
        this.tilesDownloaded = tilesDownloaded;
        this.totalTiles = totalTiles;
        this.levelTotalTiles = levelTotalTiles;
        this.timeLeftMillis = timeLeftMillis;
    }
}
