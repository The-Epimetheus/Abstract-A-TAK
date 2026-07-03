package com.atakmap.android.helloworld.features.transfer;

/**
 * Plugin-side description of a file to stage and send (see
 * {@link TransferCreator#sendFile}): where under ATAK's data root it goes,
 * and what it contains.
 */
public final class FileSendSpec {

    private final String itemPath;
    private final String contents;

    private FileSendSpec(Builder b) {
        this.itemPath = b.itemPath;
        this.contents = b.contents;
    }

    /** Destination path relative to ATAK's data root (e.g. {@code atak/}). */
    public String itemPath() {
        return itemPath;
    }

    /** File contents; encoded with the platform default charset when staged. */
    public String contents() {
        return contents;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String itemPath;
        private String contents;

        /**
         * Destination path relative to ATAK's data root, e.g.
         * {@code "tools/helloworld/sample1.hwi"}. The impl creates the
         * immediate parent directory if missing (one level only — legacy
         * used {@code mkdir()}, not {@code mkdirs()}).
         */
        public Builder itemPath(String itemPath) {
            this.itemPath = itemPath;
            return this;
        }

        /** The file contents to write. */
        public Builder contents(String contents) {
            this.contents = contents;
            return this;
        }

        public FileSendSpec build() {
            if (itemPath == null || contents == null)
                throw new IllegalStateException(
                        "itemPath and contents are required");
            return new FileSendSpec(this);
        }
    }
}
