package com.atakmap.android.helloworld.features.layers;

/**
 * Result of {@link LayerCreator#stageLayerIcon()}: whether the sample image
 * was extracted, and its destination path either way — the legacy failure
 * toast names the path, so it crosses the seam even on failure. Plugin DTO;
 * ATAK-free.
 */
public final class LayerIconDto {

    private final boolean staged;
    private final String path;

    public LayerIconDto(boolean staged, String path) {
        this.staged = staged;
        this.path = path;
    }

    /** True when the image is on disk at {@link #path()}. */
    public boolean staged() {
        return staged;
    }

    /** Absolute destination path (valid to report even when staging failed). */
    public String path() {
        return path;
    }
}
