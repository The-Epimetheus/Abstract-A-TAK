package com.atakmap.android.helloworld.features.attachment;

/**
 * Plugin DTO describing one marker-with-attachment drop — the marker to place
 * at the map center and the host-asset image to file under its attachments
 * folder. Built with the builder; immutable once built.
 */
public final class MarkerAttachmentSpec {

    private final String markerType;
    private final String callsign;
    private final String assetPath;
    private final String attachmentFileName;

    private MarkerAttachmentSpec(Builder b) {
        this.markerType = b.markerType;
        this.callsign = b.callsign;
        this.assetPath = b.assetPath;
        this.attachmentFileName = b.attachmentFileName;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** CoT type of the marker to drop (e.g. {@code "a-f-A"}). */
    public String markerType() { return markerType; }

    /** Callsign of the marker to drop. */
    public String callsign() { return callsign; }

    /**
     * Path of the source image inside the HOST APK's assets (e.g.
     * {@code "icons/ac130.png"}) — ATAK core's assets, not the plugin's.
     */
    public String assetPath() { return assetPath; }

    /** File name the attachment is stored under (e.g. {@code "test.png"}). */
    public String attachmentFileName() { return attachmentFileName; }

    public static final class Builder {
        private String markerType;
        private String callsign;
        private String assetPath;
        private String attachmentFileName;

        private Builder() {
        }

        public Builder markerType(String markerType) {
            this.markerType = markerType;
            return this;
        }

        public Builder callsign(String callsign) {
            this.callsign = callsign;
            return this;
        }

        /** Source image path inside the HOST APK's assets. */
        public Builder assetPath(String assetPath) {
            this.assetPath = assetPath;
            return this;
        }

        /** File name to store the attachment under. */
        public Builder attachmentFileName(String attachmentFileName) {
            this.attachmentFileName = attachmentFileName;
            return this;
        }

        public MarkerAttachmentSpec build() {
            return new MarkerAttachmentSpec(this);
        }
    }
}
