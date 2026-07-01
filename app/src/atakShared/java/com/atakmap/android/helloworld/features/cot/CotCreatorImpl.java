package com.atakmap.android.helloworld.features.cot;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.coremap.cot.event.CotDetail;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.cot.event.CotPoint;
import com.atakmap.coremap.maps.time.CoordinatedTime;

/**
 * The only place ATAK's CoT types are touched. Maps a {@link CotSpec} DTO to ATAK's
 * {@code CotEvent}/{@code CotPoint}/{@code CotDetail} and serializes it. Lives in
 * {@code src/atakShared} — the CoT construction API is byte-confirmed identical
 * across ATAK 4.10 -> 5.8 (see docs/analysis/byte-confirmation.md), so one impl
 * serves every version.
 */
public final class CotCreatorImpl implements CotCreator {

    @Override
    public String id() {
        return "CotCreator";
    }

    /** Single source of truth for turning a spec into a real ATAK CotEvent. */
    private CotEvent buildEvent(CotSpec spec) {
        CotEvent event = new CotEvent();
        event.setUID(spec.uid());
        event.setType(spec.type());
        event.setVersion("2.0");
        event.setHow(spec.how());
        event.setPoint(new CotPoint(spec.lat(), spec.lon(), spec.hae(), spec.ce(), spec.le()));

        CoordinatedTime now = new CoordinatedTime();
        event.setTime(now);
        event.setStart(now);
        event.setStale(now.addMinutes(spec.staleMinutes()));

        CotDetail detail = new CotDetail("detail");
        if (spec.callsign() != null) {
            CotDetail contact = new CotDetail("contact");
            contact.setAttribute("callsign", spec.callsign());
            detail.addChild(contact);
        }
        event.setDetail(detail);
        return event;
    }

    @Override
    public String buildCotXml(CotSpec spec) {
        StringBuilder sb = new StringBuilder();
        buildEvent(spec).buildXml(sb);
        return sb.toString();
    }

    /**
     * Side-effect-free, so the real op runs in full with nothing to tear down: build
     * a sample CoT under the reserved test namespace via the SAME {@link #buildEvent}
     * path used in production, validate it, and serialize it. A missing/changed ATAK
     * CoT symbol on the host surfaces here as FAILED.
     */
    @Override
    public SelfCheckResult selfCheck() {
        try {
            CotSpec spec = CotSpec.builder("HW-SELFCHECK-cot", "a-f-G", 0.0, 0.0)
                    .callsign("HW-SELFCHECK")
                    .build();
            CotEvent probe = buildEvent(spec);
            if (!probe.isValid()) {
                return SelfCheckResult.failed(id(), "CotEvent.isValid()==false", null);
            }
            String xml = buildCotXml(spec);
            if (xml == null || xml.isEmpty() || !xml.contains(spec.uid())) {
                return SelfCheckResult.failed(id(), "serialized CoT XML missing/empty", null);
            }
            return SelfCheckResult.full(id(), "built + validated + serialized CoT (" + xml.length() + " chars)");
        } catch (Throwable t) {
            return SelfCheckResult.failed(id(), "CoT construction path threw", t);
        }
    }
}
