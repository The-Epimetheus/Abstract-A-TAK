package com.atakmap.android.helloworld.navstack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.ShellProbe;
import com.atakmap.android.helloworld.plugin.R;
import com.atakmap.android.maps.MapView;

/**
 * The first real {@link ShellProbe} adapter. {@link NavigationStackDropDown} is
 * a LAZILY-created Humble shell — it is only constructed when the user taps the
 * nav-stack demo button, so a break in its ATAK base type
 * ({@code NavigationStackItem}) would otherwise surface at first tap, not at
 * load. This probe constructs the real shell at load and disposes it
 * (construction is inert: nothing is pushed onto a navigation stack).
 */
public final class NavigationStackShellProbe implements ShellProbe {

    private final Context pluginContext;

    public NavigationStackShellProbe(Context pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public String id() {
        return "NavigationStackDropDownShell";
    }

    @Override
    public SelfCheckResult probe() {
        NavigationStackDropDown shell = null;
        try {
            MapView mv = MapView.getMapView();
            if (mv == null)
                return SelfCheckResult.skipped(id(), "MapView not ready");
            View layout = LayoutInflater.from(pluginContext)
                    .inflate(R.layout.navigation_stack, null);
            shell = new NavigationStackDropDown(mv, layout, pluginContext);
            return SelfCheckResult.full(id(),
                    "constructed + disposed the real shell (never stacked)");
        } catch (Throwable t) {
            return SelfCheckResult.failed(id(),
                    "shell base type (NavigationStackItem) construct/dispose threw", t);
        } finally {
            try {
                if (shell != null)
                    shell.dispose();
            } catch (Throwable ignore) {
                // best-effort teardown
            }
        }
    }
}
