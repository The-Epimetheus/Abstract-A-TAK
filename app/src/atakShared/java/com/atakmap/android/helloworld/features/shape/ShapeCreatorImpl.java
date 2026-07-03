package com.atakmap.android.helloworld.features.shape;

import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.drawing.mapItems.DrawingCircle;
import com.atakmap.android.drawing.mapItems.DrawingEllipse;
import com.atakmap.android.drawing.mapItems.DrawingRectangle;
import com.atakmap.android.drawing.mapItems.DrawingShape;
import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.importexport.CotEventFactory;
import com.atakmap.android.maps.Association;
import com.atakmap.android.maps.DefaultMapGroup;
import com.atakmap.android.maps.Ellipse;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.MultiPolyline;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.overlay.DefaultMapGroupOverlay;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;
import com.atakmap.map.layer.feature.Feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * The only place ATAK shape drawing is touched ({@code DrawingShape} and its
 * rectangle/circle/ellipse siblings, {@code MultiPolyline},
 * {@code Association}, map groups and overlays). Source-stable across all
 * targeted versions → shared impl source set. Every {@code draw*} first
 * removes any map item carrying the spec's uid, making redraw-by-uid
 * idempotent — the ATAK quirks that made that hard (rectangles needing their
 * own child group, ellipses needing a child {@code Ellipse}) stay behind this
 * seam.
 */
public final class ShapeCreatorImpl implements ShapeCreator {

    /** ATAK's built-in group that user-drawn shapes land in. */
    private static final String DRAWING_GROUP = "Drawing Objects";

    /** Reserved test-artifact namespace (see CONTEXT.md, load-time systems check). */
    private static final String TEST_UID = "com.atakmap.android.helloworld.test.SYSTEMS_CHECK_SHAPE";

    @Override
    public String id() {
        return "ShapeCreator";
    }

    @Override
    public String drawShape(ShapeSpec spec) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return null;
        removeIfPresent(mv, spec.uid());
        DrawingShape ds = buildShape(mv, spec);
        resolveGroup(mv, spec.groupPath()).addItem(ds);
        return ds.getUID();
    }

    @Override
    public String drawRectangle(RectangleSpec spec) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return null;
        removeIfPresent(mv, spec.uid());

        MapGroup doGroup = mv.getRootGroup().findMapGroup(DRAWING_GROUP);
        // Legacy quirk: a rectangle must live in its own child group named
        // after its title; the item itself is then added to the parent.
        MapGroup child = doGroup.addGroup(
                spec.title() != null ? spec.title() : spec.uid());

        List<double[]> c = spec.corners();
        DrawingRectangle r = new DrawingRectangle(child,
                corner(c.get(0)), corner(c.get(1)),
                corner(c.get(2)), corner(c.get(3)),
                spec.uid());

        if (spec.basicStyle()) {
            r.setStyle(0);
            r.setLineStyle(0);
        }
        if (spec.fillColorArgb() != null)
            r.setFillColor(spec.fillColorArgb());
        if (spec.strokeColorArgb() != null)
            r.setStrokeColor(spec.strokeColorArgb());
        if (spec.title() != null) {
            r.setTitle(spec.title());
            r.setMetaString("shape_name", spec.title());
            r.setMetaString("callsign", spec.title());
        }
        if (spec.archive() != null)
            r.setMetaBoolean("archive", spec.archive());
        if (spec.editable() != null)
            r.setMetaBoolean("editable", spec.editable());

        doGroup.addItem(r);

        if (spec.dispatchAsCot()) {
            CotEvent cotEvent = CotEventFactory.createCotEvent(r);
            CotMapComponent.getExternalDispatcher()
                    .dispatchToBroadcast(cotEvent);
        }
        return r.getUID();
    }

    @Override
    public String drawCircle(CircleSpec spec) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return null;
        removeIfPresent(mv, spec.uid());

        DrawingCircle circle = new DrawingCircle(mv, spec.uid());
        if (spec.archive() != null)
            circle.setMetaBoolean("archive", spec.archive());
        if (spec.editable() != null)
            circle.setMetaBoolean("editable", spec.editable());
        if (spec.title() != null)
            circle.setTitle(spec.title());
        circle.setCenterPoint(GeoPointMetaData.wrap(new GeoPoint(
                spec.centerLatitude(), spec.centerLongitude())));
        if (spec.strokeColorArgb() != null)
            circle.setColor(spec.strokeColorArgb());
        if (spec.fillColorArgb() != null)
            circle.setFillColor(spec.fillColorArgb());
        circle.setRadius(spec.radiusMeters());

        mv.getRootGroup().findMapGroup(DRAWING_GROUP).addItem(circle);
        return circle.getUID();
    }

    @Override
    public String drawEllipse(EllipseSpec spec) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return null;
        removeIfPresent(mv, spec.uid());

        DrawingEllipse ellipse = new DrawingEllipse(mv, spec.uid());
        if (spec.archive() != null)
            ellipse.setMetaBoolean("archive", spec.archive());
        if (spec.editable() != null)
            ellipse.setMetaBoolean("editable", spec.editable());
        if (spec.title() != null)
            ellipse.setTitle(spec.title());
        ellipse.setCenterPoint(GeoPointMetaData.wrap(new GeoPoint(
                spec.centerLatitude(), spec.centerLongitude())));
        if (spec.strokeColorArgb() != null)
            ellipse.setColor(spec.strokeColorArgb());
        if (spec.fillColorArgb() != null)
            ellipse.setFillColor(spec.fillColorArgb());
        // A DrawingEllipse renders through at least one child Ellipse ring.
        ellipse.setEllipses(Collections.singletonList(
                new Ellipse(spec.uid() + ".1")));
        ellipse.setLength(spec.lengthMeters());
        ellipse.setWidth(spec.widthMeters());
        ellipse.setAngle(spec.angleDegrees());

        mv.getRootGroup().findMapGroup(DRAWING_GROUP).addItem(ellipse);
        return ellipse.getUID();
    }

    @Override
    public String drawMultiPolyline(MultiPolylineSpec spec) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return null;
        removeIfPresent(mv, spec.uid());
        // Also clear stray standalone items under the children's uids (e.g.
        // left over from a run where they were drawn individually).
        for (ShapeSpec child : spec.children())
            removeIfPresent(mv, child.uid());

        MapGroup group = mv.getRootGroup().findMapGroup(DRAWING_GROUP);
        List<DrawingShape> children = new ArrayList<>();
        for (ShapeSpec child : spec.children())
            children.add(buildShape(mv, child));

        MultiPolyline mp = new MultiPolyline(mv, group, children, spec.uid());
        if (spec.archive() != null)
            mp.setMetaBoolean("archive", spec.archive());
        group.addItem(mp);
        if (spec.editable() != null)
            mp.setMetaBoolean("editable", spec.editable());
        if (spec.movable() != null)
            mp.setMovable(spec.movable());
        return mp.getUID();
    }

    @Override
    public String attachAccuracyEllipse(AccuracyEllipseSpec spec) {
        final MapView mv = MapView.getMapView();
        if (mv == null)
            return null;
        MapItem item = mv.getMapItem(spec.markerUid());
        if (!(item instanceof PointMapItem))
            return null;
        final PointMapItem marker = (PointMapItem) item;

        // An Ellipse is the slower but more accurate way to draw an accuracy
        // ring; com.atakmap.android.util.Circle (center + radius) is faster.
        final double height = spec.heightMeters();
        final double width = spec.widthMeters();
        final Ellipse ellipse = new Ellipse(UUID.randomUUID().toString());
        ellipse.setCenterHeightWidth(marker.getGeoPointMetaData(), height,
                width);
        if (spec.fillColorArgb() != null) {
            ellipse.setFillColor(spec.fillColorArgb());
            ellipse.setFillStyle(2);
        }
        if (spec.strokeColorArgb() != null)
            ellipse.setStrokeColor(spec.strokeColorArgb());
        if (spec.strokeWeight() != null)
            ellipse.setStrokeWeight(spec.strokeWeight());
        if (spec.name() != null)
            ellipse.setMetaString("shapeName", spec.name());
        ellipse.setMetaBoolean("addToObjList", false);
        mv.getRootGroup().addItem(ellipse);

        // Follow the marker for life: re-center on move, leave with it.
        marker.addOnPointChangedListener(
                moved -> ellipse.setCenterHeightWidth(
                        moved.getGeoPointMetaData(), height, width));
        mv.getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_REMOVED,
                event -> {
                    if (MapEvent.ITEM_REMOVED.equals(event.getType())
                            && event.getItem().getUID()
                                    .equals(marker.getUID()))
                        mv.getRootGroup().removeItem(ellipse);
                });
        return ellipse.getUID();
    }

    @Override
    public String associate(String firstUid, String secondUid, int colorArgb) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return null;
        MapItem first = mv.getMapItem(firstUid);
        MapItem second = mv.getMapItem(secondUid);
        if (!(first instanceof PointMapItem)
                || !(second instanceof PointMapItem))
            return null;

        Association a = new Association((PointMapItem) first,
                (PointMapItem) second, UUID.randomUUID().toString());
        a.setColor(colorArgb);
        mv.getRootGroup().addItem(a);
        return a.getUID();
    }

    @Override
    public void createGroupWithOverlay(String groupName, String iconUri) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        if (mv.getRootGroup().findMapGroup(groupName) != null)
            return;

        MapGroup group = new DefaultMapGroup(groupName);
        DefaultMapGroupOverlay overlay = new DefaultMapGroupOverlay(mv, group,
                iconUri);
        mv.getRootGroup().addGroup(group);
        mv.getMapOverlayManager().addOverlay(overlay);
    }

    /* ------------------------------ internals ------------------------------ */

    private DrawingShape buildShape(MapView mv, ShapeSpec spec) {
        DrawingShape ds = new DrawingShape(mv, spec.uid());
        List<double[]> pts = spec.points();
        GeoPoint[] points = new GeoPoint[pts.size()];
        for (int i = 0; i < pts.size(); i++) {
            double[] p = pts.get(i);
            points[i] = Double.isNaN(p[2])
                    ? new GeoPoint(p[0], p[1])
                    : new GeoPoint(p[0], p[1], p[2]);
        }
        ds.setPoints(points);
        if (spec.title() != null)
            ds.setTitle(spec.title());
        if (spec.closed())
            ds.setClosed(true);
        if (spec.strokeColorArgb() != null)
            ds.setStrokeColor(spec.strokeColorArgb());
        if (spec.fillColorArgb() != null)
            ds.setFillColor(spec.fillColorArgb());
        if (spec.heightMeters() != null)
            ds.setHeight(spec.heightMeters());
        if (spec.movable() != null)
            ds.setMovable(spec.movable());
        if (spec.clickable())
            ds.setClickable(true);
        // setMetaBoolean("editable") gates the edit capability; the similarly
        // named setEditable() would START edit mode — not what a spec means.
        if (spec.editable() != null)
            ds.setMetaBoolean("editable", spec.editable());
        if (spec.archive() != null)
            ds.setMetaBoolean("archive", spec.archive());
        switch (spec.altitudeMode()) {
            case ABSOLUTE:
                ds.setAltitudeMode(Feature.AltitudeMode.Absolute);
                break;
            case CLAMP_TO_GROUND:
                ds.setAltitudeMode(Feature.AltitudeMode.ClampToGround);
                break;
            case DEFAULT:
                break;
        }
        for (java.util.Map.Entry<String, Boolean> e : spec.metaBooleans()
                .entrySet()) {
            ds.setMetaBoolean(e.getKey(), e.getValue());
        }
        return ds;
    }

    private MapGroup resolveGroup(MapView mv, String[] path) {
        MapGroup group = mv.getRootGroup();
        if (path == null)
            return group.findMapGroup(DRAWING_GROUP);
        for (String name : path)
            group = group.findMapGroup(name);
        return group;
    }

    private static GeoPointMetaData corner(double[] latLon) {
        return GeoPointMetaData.wrap(new GeoPoint(latLon[0], latLon[1]));
    }

    private static void removeIfPresent(MapView mv, String uid) {
        MapItem item = mv.getMapItem(uid);
        if (item != null)
            item.removeFromGroup();
    }

    /**
     * Draws a real (invisible-to-overlays) shape under the reserved test
     * namespace and removes it in a finally — the full build/group/add path
     * runs (FULL), nothing persists.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "shape drawing path threw", () -> {
            MapView mv = MapView.getMapView();
            if (mv == null)
                return SelfCheckResult.skipped(id(), "MapView not ready");
            MapGroup group = mv.getRootGroup().findMapGroup(DRAWING_GROUP);
            if (group == null)
                return SelfCheckResult.skipped(id(),
                        "drawing group not ready");
            DrawingShape probe = null;
            try {
                probe = new DrawingShape(mv, TEST_UID);
                probe.setPoints(new GeoPoint[] {
                        new GeoPoint(0, 0), new GeoPoint(0.0001, 0.0001)
                });
                probe.setMetaBoolean("addToObjList", false);
                probe.setMetaBoolean("archive", false);
                group.addItem(probe);
                return SelfCheckResult.full(id(),
                        "drew + removed a test shape (not persisted)");
            } finally {
                if (probe != null && probe.getGroup() != null)
                    probe.removeFromGroup();
            }
        });
    }
}
