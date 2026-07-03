package com.atakmap.android.helloworld.features.marker;

import android.content.Intent;
import android.os.SystemClock;

import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.icons.UserIcon;
import com.atakmap.android.importexport.CotEventFactory;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.user.PlacePointTool;
import com.atakmap.coremap.maps.assets.Icon;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;
import com.atakmap.coremap.maps.time.CoordinatedTime;

import java.util.Map;

/**
 * The only place ATAK marker placement is touched ({@code Marker},
 * {@code PlacePointTool}, map groups, {@code Icon}). Source-stable across all
 * targeted versions → shared impl source set. A {@link MarkerSpec} with
 * {@code dropAsIfByUser} goes through the point tool (user-drop semantics);
 * everything else is a programmatic construct-and-add.
 */
public final class MarkerCreatorImpl implements MarkerCreator {

    /** Reserved test-artifact namespace (see CONTEXT.md, load-time systems check). */
    private static final String TEST_UID = "com.atakmap.android.helloworld.test.SYSTEMS_CHECK_MARKER";

    @Override
    public String id() {
        return "MarkerCreator";
    }

    @Override
    public String placeMarker(MarkerSpec spec) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return null;

        GeoPointMetaData point = spec.isAtMapCenter()
                ? mv.getPointWithElevation()
                : GeoPointMetaData.wrap(
                        new GeoPoint(spec.latitude(), spec.longitude()));

        Marker m = spec.dropAsIfByUser()
                ? dropViaPointTool(spec, point)
                : constructAndAdd(mv, spec, point);

        applyExtras(mv, m, spec);
        return m.getUID();
    }

    @Override
    public boolean markerExists(String uid) {
        MapView mv = MapView.getMapView();
        return mv != null && mv.getMapItem(uid) != null;
    }

    @Override
    public boolean moveMarkerWithTrack(String uid, double latitude,
            double longitude, String lastUpdateMetaKey) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return false;
        final MapItem mi = mv.getMapItem(uid);
        if (mi == null)
            return false;
        if (mi instanceof Marker) {
            Marker marker = (Marker) mi;

            GeoPoint newPoint = new GeoPoint(latitude, longitude);
            GeoPoint lastPoint = marker.getPoint();
            long currTime = SystemClock.elapsedRealtime();

            double dist = lastPoint.distanceTo(newPoint);
            double dir = lastPoint.bearingTo(newPoint);

            double delta = currTime -
                    mi.getMetaLong(lastUpdateMetaKey, 0);

            // The first move after placement reads meta 0, so delta spans
            // the whole boot uptime and speed comes out ~0; the next poll
            // yields the real track. Same math as the legacy demo.
            double speed = dist / (delta / 1000f);

            marker.setTrack(dir, speed);

            marker.setPoint(newPoint);
            mi.setMetaLong(lastUpdateMetaKey,
                    SystemClock.elapsedRealtime());
        }
        return true;
    }

    @Override
    public boolean setMarkerTextColor(String uid, int argb) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return false;
        MapItem item = mv.getMapItem(uid);
        if (!(item instanceof Marker))
            return false;
        // Live label restyle: takes effect immediately, no refresh needed.
        ((Marker) item).setTextColor(argb);
        return true;
    }

    private Marker dropViaPointTool(MarkerSpec spec, GeoPointMetaData point) {
        PlacePointTool.MarkerCreator mc = new PlacePointTool.MarkerCreator(
                point);
        mc.setUid(spec.uid());
        if (spec.callsign() != null)
            mc.setCallsign(spec.callsign());
        if (spec.type() != null)
            mc.setType(spec.type());
        mc.showCotDetails(spec.showCotDetails());
        mc.setNeverPersist(spec.neverPersist());
        if (spec.archive() != null)
            mc.setArchive(spec.archive());
        return mc.placePoint();
    }

    private Marker constructAndAdd(MapView mv, MarkerSpec spec,
            GeoPointMetaData point) {
        Marker m = new Marker(point, spec.uid());
        if (spec.type() != null)
            m.setType(spec.type());
        if (spec.title() != null)
            m.setTitle(spec.title());
        if (spec.menu() != null)
            m.setMetaString("menu", spec.menu());
        if (spec.alwaysShowText())
            m.setAlwaysShowText(true);
        if (spec.clickable())
            m.setClickable(true);

        MapGroup group = mv.getRootGroup();
        if (spec.groupPath() != null) {
            for (String name : spec.groupPath()) {
                group = group.findMapGroup(name);
            }
        }
        group.addItem(m);

        if (spec.persistAndAnnounce()) {
            m.persist(mv.getMapEventDispatcher(), null, getClass());
            Intent placed = new Intent();
            placed.setAction("com.atakmap.android.maps.COT_PLACED");
            placed.putExtra("uid", m.getUID());
            AtakBroadcast.getInstance().sendBroadcast(placed);
        }
        return m;
    }

    /** Extras shared by both placement paths (style, track, color, iconset, meta). */
    private void applyExtras(MapView mv, Marker m, MarkerSpec spec) {
        boolean refresh = false;
        if (spec.iconUri() != null) {
            m.setIcon(new Icon.Builder().setImageUri(0, spec.iconUri())
                    .build());
            refresh = true;
        }
        if (spec.showLabel()) {
            m.setShowLabel(true);
            refresh = true;
        }
        if (spec.fullRotation()) {
            // Default style shows an arrow; freeing rotation needs the heading
            // mask AND the no-arrow mask.
            m.setStyle(m.getStyle()
                    | Marker.STYLE_ROTATE_HEADING_MASK
                    | Marker.STYLE_ROTATE_HEADING_NOARROW_MASK);
            refresh = true;
        }
        if (spec.rotateWithHeading()) {
            m.setStyle(m.getStyle() | Marker.STYLE_ROTATE_HEADING_MASK);
            refresh = true;
        }
        if (spec.autoStaleMillis() != null) {
            m.setMetaLong("lastUpdateTime",
                    new CoordinatedTime().getMilliseconds());
            m.setMetaLong("autoStaleDuration", spec.autoStaleMillis());
            refresh = true;
        }
        if (spec.trackHeading() != null && spec.trackSpeed() != null) {
            m.setTrack(spec.trackHeading(), spec.trackSpeed());
            refresh = true;
        }
        if (spec.colorArgb() != null) {
            m.setMetaInteger("color", spec.colorArgb());
            refresh = true;
        }
        if (spec.iconsetPath() != null) {
            m.setMetaString(UserIcon.IconsetPath, spec.iconsetPath());
            refresh = true;
        }
        for (Map.Entry<String, Boolean> e : spec.metaBooleans().entrySet()) {
            m.setMetaBoolean(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, String> e : spec.metaStrings().entrySet()) {
            m.setMetaString(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Long> e : spec.metaLongs().entrySet()) {
            m.setMetaLong(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Double> e : spec.metaDoubles().entrySet()) {
            m.setMetaDouble(e.getKey(), e.getValue());
        }
        if (refresh) {
            m.refresh(mv.getMapEventDispatcher(), null, getClass());
        }
        if (spec.persist()) {
            m.persist(mv.getMapEventDispatcher(), null, getClass());
        }
        if (spec.dispatchAsCot()) {
            CotEvent cotEvent = CotEventFactory.createCotEvent(m);
            CotMapComponent.getExternalDispatcher()
                    .dispatchToBroadcast(cotEvent);
        }
    }

    /**
     * Places a real marker under the reserved test namespace and removes it in
     * a finally — the full construct/group/add path runs (FULL), nothing
     * persists and nothing is announced.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "marker placement path threw", () -> {
            MapView mv = MapView.getMapView();
            if (mv == null)
                return SelfCheckResult.skipped(id(), "MapView not ready");
            Marker probe = null;
            try {
                probe = new Marker(mv.getPointWithElevation(), TEST_UID);
                probe.setType("a-f-G");
                probe.setTitle("systems-check probe");
                mv.getRootGroup().addItem(probe);
                return SelfCheckResult.full(id(),
                        "placed + removed a test marker (not persisted)");
            } finally {
                if (probe != null && probe.getGroup() != null)
                    probe.removeFromGroup();
            }
        });
    }
}
