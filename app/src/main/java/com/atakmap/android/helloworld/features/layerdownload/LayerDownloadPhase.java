package com.atakmap.android.helloworld.features.layerdownload;

/**
 * Plugin-owned job phase, mapped by the impl from ATAK's {@code DownloadJob}
 * status codes so those constants never cross the seam.
 */
public enum LayerDownloadPhase {
    CONNECTING,
    DOWNLOADING,
    COMPLETE,
    ERROR,
    CANCELLED
}
