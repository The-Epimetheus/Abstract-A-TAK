package com.atakmap.android.helloworld.abstraction;

import java.util.Objects;
import java.util.UUID;

/**
 * An opaque, plugin-owned reference to a live ATAK object the plugin created and
 * retains (a Marker, Layer, DropDown, ...). {@code src/main} holds Handles, never
 * the ATAK object itself; the Creator implementation keeps a {@code Handle -> ATAK
 * object} registry and evicts on delete.
 *
 * <p>Contrast with a Plugin DTO, which carries an ATAK object's data <em>by value</em>;
 * a Handle is a <em>reference</em> to a live object. ATAK-free; lives in {@code src/main}.
 *
 * <p>Feature Creators typically subclass this into a typed handle (e.g.
 * {@code MarkerHandle}) so the type system keeps a Marker handle from being passed
 * where a Layer handle is expected.
 */
public abstract class Handle {

    private final UUID token;

    protected Handle() {
        this.token = UUID.randomUUID();
    }

    /** The opaque token the owning Creator implementation resolves to a live object. */
    public final UUID token() {
        return token;
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof Handle && ((Handle) o).token.equals(this.token);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(token);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + token + ")";
    }
}
