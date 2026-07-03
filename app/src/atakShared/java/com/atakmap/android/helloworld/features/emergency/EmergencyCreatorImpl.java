package com.atakmap.android.helloworld.features.emergency;

import com.atakmap.android.emergency.tool.EmergencyManager;
import com.atakmap.android.emergency.tool.EmergencyType;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;

/**
 * The only place {@code EmergencyManager} is touched. Source-stable across
 * all targeted versions → shared impl source set.
 */
public final class EmergencyCreatorImpl implements EmergencyCreator {

    @Override
    public String id() {
        return "EmergencyCreator";
    }

    @Override
    public void start911Alert() {
        EmergencyManager.getInstance().initiateRepeat(EmergencyType.NineOneOne,
                false);
    }

    @Override
    public void cancel911Alert() {
        EmergencyManager.getInstance().cancelRepeat(EmergencyType.NineOneOne,
                false);
    }

    @Override
    public void startAlertOfType(String typeDescription) {
        // Verbatim from the legacy speech dispatch: set the current type,
        // start the repeat, then flag the emergency on. (Legacy resolved the
        // description twice; folded to one lookup — same value.)
        EmergencyType type = EmergencyType.fromDescription(typeDescription);
        EmergencyManager.getInstance().setEmergencyType(type);
        EmergencyManager.getInstance().initiateRepeat(type, false);
        EmergencyManager.getInstance().setEmergencyOn(true);
    }

    /**
     * PARTIAL by design: actually initiating a 911 alert would broadcast to
     * the whole team — irreversible and very visible — so the probe only
     * resolves the manager and the alert type on this host.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "emergency manager path threw", () -> {
            EmergencyManager mgr = EmergencyManager.getInstance();
            if (mgr == null)
                return SelfCheckResult.skipped(id(),
                        "EmergencyManager not ready");
            EmergencyType type = EmergencyType.NineOneOne;
            return SelfCheckResult.partial(id(),
                    "manager + type '" + type.getDescription()
                            + "' resolved; not initiated (would alert the network)");
        });
    }
}
