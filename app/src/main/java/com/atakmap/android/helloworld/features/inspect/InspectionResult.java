package com.atakmap.android.helloworld.features.inspect;

/**
 * What came back from inspecting one selected map item: its CoT (Cursor on
 * Target) representation, when the host could produce one. Not every map item
 * converts — items flagged {@code nevercot} are deliberately never persisted
 * or shared as CoT, and some item types simply have no CoT marshal.
 */
public final class InspectionResult {

    private final String cotXml;
    private final boolean neverCot;

    public InspectionResult(String cotXml, boolean neverCot) {
        this.cotXml = cotXml;
        this.neverCot = neverCot;
    }

    /**
     * The item rendered as a CoT event (XML), or {@code null} when the
     * conversion produced nothing — see {@link #neverCot()} for why.
     */
    public String cotXml() {
        return cotXml;
    }

    /**
     * True when the item is flagged to never persist as CoT ({@code nevercot})
     * — the expected reason a {@link #cotXml()} is null; a null CoT without
     * this flag is a conversion error.
     */
    public boolean neverCot() {
        return neverCot;
    }
}
