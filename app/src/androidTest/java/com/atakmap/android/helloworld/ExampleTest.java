
package com.atakmap.android.helloworld;

import com.atakmap.android.test.helpers.ATAKTestClass;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import android.os.Build;

public class ExampleTest extends ATAKTestClass {
    private final HelloWorldRobot helloWorldRobot = new HelloWorldRobot();

    @BeforeClass
    public static void setupPlugin() {
        // These tests drive the plugin through the ATAK UI (espresso); unlike
        // SystemsCheckTest they never read plugin statics across the classloader
        // split, so they do not need the harness's ClassLoaderReplacer — which
        // reflects on AtakPluginRegistry.loadedSourceDirs, a field renamed to
        // pkgNameClassLoader in ATAK 5.8 and thus broken there.
        HelloWorldRobot.installPlugin();
    }

    @After
    public void cleanupAfterEachTest() {
        // Code to run between each test, to attempt to reset the state. Adjust as needed for your tests.
        helper.pressBackTimes(5);
        helper.deleteAllMarkers();
    }

    @Ignore("Fires a real 911 emergency alert (EmergencyManager.initiateRepeat) "
            + "— the network-visible effect EmergencyCreator's selfCheck refuses "
            + "to trigger — and crashes the ATAK process on a CI device with no "
            + "GPS self-position. The emergency feature is covered by the "
            + "load-time systems check (see SystemsCheckTest).")
    @Test
    public void testEmergencyButtons() {
        helloWorldRobot
                .openToolFromOverflow()
                .pressEmergencyButton()
                .verifyEmergencyMarkerExists()
                .pressNoEmergencyButton()
                .verifyNoEmergencyMarkerExists();
    }

    @Test
    public void testAddAircraftButton() {
        String expectedName = "SNF";
        helloWorldRobot
                .openToolFromOverflow()
                .pressAddAnAircraftButton()
                .verifyAircraftMarkerWithNameExists(expectedName)
                .pressAircraftDetailsRadialMenuButton()
                .verifyMarkerDetailsName(expectedName);
    }

    @Test
    public void testTrackSpaceStation() {

        // The current ISS plotting site uses cleartext http connection and offers no https ability.
        // Since this is not allowed on Android 9 or higher, do not test this capability.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            return;

        helloWorldRobot
                .openToolFromOverflow()
                .pressISSButton()
                .verifyISSExists();
    }
}
