package com.atakmap.android.helloworld.features.toolbar;

import android.content.Intent;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.tools.ActionBarReceiver;
import com.atakmap.android.tools.menu.ActionBroadcastData;
import com.atakmap.android.tools.menu.ActionBroadcastExtraStringData;
import com.atakmap.android.tools.menu.ActionClickData;
import com.atakmap.android.tools.menu.ActionMenuData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The only place the {@code ActionMenuData} family and
 * {@code ActionBarReceiver}'s ADD_NEW_TOOLS / REMOVE_TOOLS broadcasts are
 * touched. Source-stable across all targeted versions → shared impl source
 * set. There is no direct API for toolbar items: everything goes through
 * broadcasts on ATAK's internal bus, with the item description serialized as
 * an {@code ActionMenuData[]} intent extra named {@code "menus"}.
 */
public final class ToolbarCreatorImpl implements ToolbarCreator {

    /** Reserved test-artifact namespace (built only, never dispatched). */
    private static final String TEST_REF = "com.atakmap.android.helloworld.test/SystemsCheckTool";

    /**
     * ref → the exact {@code ActionMenuData} instance ADD_NEW_TOOLS
     * dispatched. REMOVE_TOOLS hands the same instance back — the legacy demo
     * built one instance and reused it for add and remove, and matching by an
     * equal-looking fresh instance is not a contract ATAK documents, so the
     * seam does not rely on it.
     */
    private final Map<String, ActionMenuData> added = new HashMap<>();

    @Override
    public String id() {
        return "ToolbarCreator";
    }

    @Override
    public void addToolbarItem(ToolbarItemSpec spec) {
        ActionMenuData amd = buildMenuEntry(spec);
        added.put(spec.ref(), amd);
        Intent intent = new Intent(ActionBarReceiver.ADD_NEW_TOOLS);
        intent.putExtra("menus", new ActionMenuData[] {
                amd
        });
        AtakBroadcast.getInstance().sendBroadcast(intent);
    }

    @Override
    public void removeToolbarItem(String ref) {
        ActionMenuData amd = added.remove(ref);
        if (amd == null)
            return; // never added this load — nothing to hand back to ATAK
        Intent intent = new Intent(ActionBarReceiver.REMOVE_TOOLS);
        intent.putExtra("menus", new ActionMenuData[] {
                amd
        });
        AtakBroadcast.getInstance().sendBroadcast(intent);
    }

    private static ActionMenuData buildMenuEntry(ToolbarItemSpec spec) {
        // The tap fires a broadcast that carries no extras. (ATAK can attach
        // per-click string extras via ActionBroadcastExtraStringData, but that
        // constructor is unproven across the ten targeted versions, so the
        // seam does not offer extras.)
        ActionBroadcastData abd = new ActionBroadcastData(
                spec.clickBroadcastAction(),
                new ArrayList<ActionBroadcastExtraStringData>());

        // "click" names the plain-tap gesture the broadcast is bound to.
        List<ActionClickData> acdList = new ArrayList<>();
        acdList.add(new ActionClickData(abd, "click"));

        // Positional ActionMenuData constructor, called with exactly the
        // argument shape the legacy demo used (proven across all ten targeted
        // versions): ref, title, icon name, two unused icon slots, preferred
        // placement string, then the click actions sandwiched between the
        // legacy demo's false flags.
        return new ActionMenuData(spec.ref(), spec.title(), spec.iconName(),
                null, null, placementValue(spec.placement()), false, acdList,
                false, false, false);
    }

    /** Plugin-owned placement enum → ATAK's free-form preferred-menu string. */
    private static String placementValue(ToolbarPlacement placement) {
        // Only OVERFLOW exists today; the switch is where a proven new
        // placement string would land.
        switch (placement) {
            case OVERFLOW:
            default:
                return "overflow";
        }
    }

    /**
     * PARTIAL by design: builds the full ActionMenuData chain under the
     * reserved test namespace and resolves both ActionBarReceiver actions —
     * the version-sensitive symbols this impl links — but dispatches nothing:
     * a real ADD_NEW_TOOLS visibly changes the user's toolbar, and REMOVE is
     * only its undo after the fact.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "ActionMenuData family threw", () -> {
            ActionMenuData probe = buildMenuEntry(ToolbarItemSpec.builder()
                    .ref(TEST_REF)
                    .title("Systems Check Tool")
                    .iconName("ic_menu_drawing")
                    .clickBroadcastAction(
                            "com.atakmap.android.helloworld.test.TOOLBAR_PING")
                    .placement(ToolbarPlacement.OVERFLOW)
                    .build());
            String actions = ActionBarReceiver.ADD_NEW_TOOLS + " / "
                    + ActionBarReceiver.REMOVE_TOOLS;
            return SelfCheckResult.partial(id(),
                    "built " + probe.getClass().getSimpleName()
                            + " under the reserved test ref; actions resolved ("
                            + actions
                            + "); not dispatched (user-visible toolbar change)");
        });
    }
}
