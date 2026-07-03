package com.atakmap.android.helloworld.features.datapackage;

/**
 * Plugin-side description of a "package the items around me" build (see
 * {@link DataPackageCreator#buildNearbyItemsPackage}): the package name shown
 * in ATAK, the absolute path the zip is written to, and the search radius
 * around the self marker.
 */
public final class NearbyItemsPackageSpec {

    private final String name;
    private final String filePath;
    private final int radiusMeters;

    private NearbyItemsPackageSpec(Builder b) {
        this.name = b.name;
        this.filePath = b.filePath;
        this.radiusMeters = b.radiusMeters;
    }

    /** The package name (manifest name shown in ATAK's Mission Package tool). */
    public String name() {
        return name;
    }

    /** Absolute path the package zip is written to. */
    public String filePath() {
        return filePath;
    }

    /** Search radius around the self marker, in meters. */
    public int radiusMeters() {
        return radiusMeters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private String filePath;
        private int radiusMeters;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder radiusMeters(int radiusMeters) {
            this.radiusMeters = radiusMeters;
            return this;
        }

        public NearbyItemsPackageSpec build() {
            if (name == null || filePath == null)
                throw new IllegalStateException(
                        "name and filePath are required");
            if (radiusMeters <= 0)
                throw new IllegalStateException(
                        "radiusMeters must be positive");
            return new NearbyItemsPackageSpec(this);
        }
    }
}
