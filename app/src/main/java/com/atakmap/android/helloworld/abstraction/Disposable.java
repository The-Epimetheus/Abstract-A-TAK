package com.atakmap.android.helloworld.abstraction;

/**
 * A feature Controller that holds live registrations — broadcast listeners,
 * timers, sensor hooks, layers on the map — and must tear them down when the
 * plugin unloads (ATAK hot-reloads plugins; leaked registrations survive into
 * the next load). {@code PaneController.dispose()} cascades to every feature
 * Controller that implements this; stateless pass-through Controllers don't.
 */
public interface Disposable {

    /** Tear down everything this Controller registered. Must be idempotent. */
    void dispose();
}
