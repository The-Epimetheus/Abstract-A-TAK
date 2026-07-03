package com.atakmap.android.helloworld.features.toolbar;

/**
 * Where a toolbar item prefers to appear — the plugin-owned stand-in for the
 * free-form "preferred menu" string ATAK's {@code ActionMenuData} takes.
 * Only the placement the legacy demo exercised is offered: ATAK accepts other
 * strings, but they are unproven across the ten targeted versions, so the
 * seam does not advertise them (compile-risk discipline).
 */
public enum ToolbarPlacement {

    /** The action bar's overflow (three-dot) menu. */
    OVERFLOW
}
