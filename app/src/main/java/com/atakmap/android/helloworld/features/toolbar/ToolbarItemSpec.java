package com.atakmap.android.helloworld.features.toolbar;

/**
 * Plugin-side description of one action-bar toolbar item (see
 * {@link ToolbarCreator#addToolbarItem}). Carries exactly what the legacy
 * demo fed ATAK's {@code ActionMenuData}: an identifying ref, a display
 * title, an icon name, the broadcast a tap fires, and a placement. The tap
 * broadcast carries no extras — ATAK supports per-click string extras, but
 * the seam does not offer them (unproven across the targeted versions).
 */
public final class ToolbarItemSpec {

    private final String ref;
    private final String title;
    private final String iconName;
    private final String clickBroadcastAction;
    private final ToolbarPlacement placement;

    private ToolbarItemSpec(Builder b) {
        this.ref = b.ref;
        this.title = b.title;
        this.iconName = b.iconName;
        this.clickBroadcastAction = b.clickBroadcastAction;
        this.placement = b.placement;
    }

    public String ref() {
        return ref;
    }

    public String title() {
        return title;
    }

    public String iconName() {
        return iconName;
    }

    public String clickBroadcastAction() {
        return clickBroadcastAction;
    }

    public ToolbarPlacement placement() {
        return placement;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String ref;
        private String title;
        private String iconName;
        private String clickBroadcastAction;
        private ToolbarPlacement placement = ToolbarPlacement.OVERFLOW;

        /**
         * The item's identifying ref, {@code "<group>/<name>"} style (the
         * demo uses {@code "com.ford.tool/TowTruck"}). Also the key a later
         * {@link ToolbarCreator#removeToolbarItem(String)} removes by.
         */
        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        /** Display title shown next to the icon. */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Name of an icon drawable ATAK can resolve (the demo uses ATAK's
         * stock {@code "ic_menu_drawing"}).
         */
        public Builder iconName(String iconName) {
            this.iconName = iconName;
            return this;
        }

        /**
         * Intent action fired on ATAK's internal broadcast bus (no extras)
         * when the toolbar item is tapped.
         */
        public Builder clickBroadcastAction(String action) {
            this.clickBroadcastAction = action;
            return this;
        }

        /** Preferred placement; defaults to {@link ToolbarPlacement#OVERFLOW}. */
        public Builder placement(ToolbarPlacement placement) {
            this.placement = placement;
            return this;
        }

        public ToolbarItemSpec build() {
            if (ref == null || title == null || iconName == null
                    || clickBroadcastAction == null)
                throw new IllegalStateException(
                        "ref, title, iconName and clickBroadcastAction are required");
            return new ToolbarItemSpec(this);
        }
    }
}
