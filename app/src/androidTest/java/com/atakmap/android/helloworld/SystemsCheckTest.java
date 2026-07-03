package com.atakmap.android.helloworld;

import android.app.UiAutomation;
import android.os.ParcelFileDescriptor;

import androidx.test.platform.app.InstrumentationRegistry;

import com.atakmap.android.test.helpers.ATAKTestClass;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The load-time systems check's second consumer (the first is the log writer):
 * loads the plugin on the real host and asserts the load-run's report has no
 * FAILED item — ADR-0004's fast loop, machine-checked instead of eyeballed in
 * Logcat by hand.
 *
 * <p>The report is read back from Logcat (the plugin logs the full CheckReport
 * summary at load under the {@code HelloWorldSystemsCheck} tag) rather than from
 * {@link com.atakmap.android.helloworld.abstraction.SystemsCheck}{@code
 * .latestReport()}. Reading that retained object across the test/plugin
 * classloader split needs ATAK's plugin-classloader registry, whose internals
 * are renamed between ATAK versions — {@code AtakPluginRegistry.loadedSourceDirs}
 * became {@code pkgNameClassLoader} in 5.8, which breaks the test harness's
 * reflection-based {@code ClassLoaderReplacer}. Coupling the test to those
 * private fields is exactly the version fragility this project abstracts away;
 * the logged summary is the version-stable public contract, so we assert on it.
 */
public class SystemsCheckTest extends ATAKTestClass {

    /** Matches e.g. "systems check: 30 items in 112ms — … FAILED=0". */
    private static final Pattern SUMMARY = Pattern.compile(
            "systems check: (\\d+) items.*FAILED=(\\d+)");

    @BeforeClass
    public static void setupPlugin() {
        // Loads the plugin through the real Plugin Manager UI; the systems
        // check runs inside HelloWorldMapComponent.onCreate as it loads.
        HelloWorldRobot.installPlugin();
    }

    @Test
    public void loadRunReportHasNoFailures() {
        // Poll Logcat until the load run's summary line appears (the load is
        // asynchronous relative to installPlugin returning).
        String summary = helper.nullWait(new Callable<String>() {
            @Override
            public String call() {
                return latestSystemsCheckSummary();
            }
        }, 30000);

        Assert.assertNotNull(
                "systems check never logged — plugin did not finish loading",
                summary);

        Matcher m = SUMMARY.matcher(summary);
        Assert.assertTrue("unrecognized systems-check summary: " + summary,
                m.find());
        int items = Integer.parseInt(m.group(1));
        int failed = Integer.parseInt(m.group(2));

        Assert.assertTrue("systems check swept zero items", items > 0);
        Assert.assertEquals(
                "systems check reported FAILED item(s) on this host: " + summary,
                0, failed);
    }

    /**
     * The most recent {@code HelloWorldSystemsCheck} summary line in Logcat, or
     * {@code null} if none is buffered yet.
     */
    private static String latestSystemsCheckSummary() {
        UiAutomation ua = InstrumentationRegistry.getInstrumentation()
                .getUiAutomation();
        ParcelFileDescriptor pfd = ua.executeShellCommand(
                "logcat -d -s HelloWorldSystemsCheck:I");
        String last = null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(pfd.getFileDescriptor())))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.contains("systems check:"))
                    last = line;
            }
        } catch (Exception e) {
            return null;
        }
        return last;
    }
}
