package com.atakmap.android.helloworld.features.shape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Plugin DTO for a multi-polyline: several freeform shapes managed as one map
 * item. Built with the builder; immutable once built.
 */
public final class MultiPolylineSpec {

    private final String uid;
    private final List<ShapeSpec> children;
    private final Boolean movable;
    private final Boolean editable;
    private final Boolean archive;

    private MultiPolylineSpec(Builder b) {
        this.uid = b.uid != null ? b.uid : UUID.randomUUID().toString();
        this.children = Collections
                .unmodifiableList(new ArrayList<>(b.children));
        this.movable = b.movable;
        this.editable = b.editable;
        this.archive = b.archive;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String uid() { return uid; }
    public List<ShapeSpec> children() { return children; }
    public Boolean movable() { return movable; }
    public Boolean editable() { return editable; }
    public Boolean archive() { return archive; }

    public static final class Builder {
        private String uid;
        private final List<ShapeSpec> children = new ArrayList<>();
        private Boolean movable;
        private Boolean editable;
        private Boolean archive;

        private Builder() {
        }

        public Builder uid(String uid) { this.uid = uid; return this; }

        /** Append one child line; group-path settings on it are ignored. */
        public Builder child(ShapeSpec shape) { children.add(shape); return this; }

        public Builder movable(boolean movable) { this.movable = movable; return this; }
        public Builder editable(boolean editable) { this.editable = editable; return this; }
        public Builder archive(boolean archive) { this.archive = archive; return this; }

        public MultiPolylineSpec build() {
            return new MultiPolylineSpec(this);
        }
    }
}
