package com.atakmap.android.helloworld.features.emergency;

/**
 * The emergency feature's Controller. A bare pass-through to
 * {@link EmergencyCreator} — kept anyway so every feature shows the same
 * tap → Controller → Creator shape (ADR-0005).
 */
public class EmergencyController {

    private final EmergencyCreator emergencyCreator;

    public EmergencyController(EmergencyCreator emergencyCreator) {
        this.emergencyCreator = emergencyCreator;
    }

    public void startEmergency() {
        emergencyCreator.start911Alert();
    }

    public void cancelEmergency() {
        emergencyCreator.cancel911Alert();
    }
}
