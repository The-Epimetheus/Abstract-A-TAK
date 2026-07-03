package com.atakmap.android.helloworld.features.shape;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's drawing-shape cluster ({@code DrawingShape},
 * rectangle/circle/ellipse map items, multi-polylines, associations, map
 * groups and overlays). Every {@code draw*} call <b>replaces</b> any existing
 * map item with the spec's uid — redraw-by-uid is idempotent, so callers never
 * clean up a previous run. Interface in {@code src/main}; the implementation
 * lives in {@code src/atakShared}.
 */
public interface ShapeCreator extends Creator {

    /**
     * Draw the freeform shape (polyline or closed polygon) described by
     * {@code spec}, replacing any map item with the same uid.
     *
     * @return the shape's uid, or null if the map is not ready.
     */
    String drawShape(ShapeSpec spec);

    /** Draw a rectangle; same uid-replacement contract as drawShape. */
    String drawRectangle(RectangleSpec spec);

    /** Draw a circle; same uid-replacement contract as drawShape. */
    String drawCircle(CircleSpec spec);

    /** Draw an ellipse; same uid-replacement contract as drawShape. */
    String drawEllipse(EllipseSpec spec);

    /**
     * Draw several lines as one multi-polyline map item; replaces any item
     * with the spec's uid AND any stray items left under its children's uids.
     */
    String drawMultiPolyline(MultiPolylineSpec spec);

    /**
     * Attach an accuracy ellipse to an existing marker: it re-centers as the
     * marker moves and is removed from the map when the marker is removed.
     *
     * @return the ellipse's uid, or null if the marker was not found.
     */
    String attachAccuracyEllipse(AccuracyEllipseSpec spec);

    /**
     * Draw a line associating two existing point items (by uid).
     *
     * @return the association's uid, or null if either point was not found.
     */
    String associate(String firstUid, String secondUid, int colorArgb);

    /**
     * Create a named map group with its own overlay-manager entry (icon from
     * {@code iconUri}), so items placed under {@code groupPath(name)} get
     * their own toggleable layer. No-op if the group already exists.
     */
    void createGroupWithOverlay(String groupName, String iconUri);
}
