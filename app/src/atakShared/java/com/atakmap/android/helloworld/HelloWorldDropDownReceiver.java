
package com.atakmap.android.helloworld;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.chat.ChatManagerMapComponent;
import com.atakmap.android.contact.Connector;
import com.atakmap.android.contact.Contact;
import com.atakmap.android.contact.Contacts;
import com.atakmap.android.contact.IndividualContact;
import com.atakmap.android.contact.IpConnector;
import com.atakmap.android.contact.PluginConnector;
import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownManager;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.helloworld.features.layerdownload.LayerDownloadCreator;
import com.atakmap.android.helloworld.features.pane.PaneController;
import com.atakmap.android.helloworld.features.pane.PaneShell;
import com.atakmap.android.helloworld.layers.LayerDownloadExample;
import com.atakmap.android.helloworld.navstack.NavigationStackDropDown;
import com.atakmap.android.helloworld.plugin.R;
import com.atakmap.android.helloworld.recyclerview.RecyclerViewDropDown;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.navigationstack.DropDownNavigationStack;
import com.atakmap.android.preference.AtakPreferences;
import com.atakmap.android.util.ATAKUtilities;
import com.atakmap.comms.CommsMapComponent;
import com.atakmap.comms.CotServiceRemote;
import com.atakmap.comms.CotStreamListener;
import com.atakmap.comms.NetConnectString;
import com.atakmap.comms.TAKServer;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;
import java.util.List;
import java.util.UUID;

/**
 * The DropDown Receiver should define the visual experience
 * that a user might have while using this plugin.   At a
 * basic level, the dropdown can be a view of your own design
 * that is inflated.   Please be wary of the type of context
 * you use.   As noted in the Map Component, there are two
 * contexts - the plugin context and the atak context.
 * When using the plugin context - you cannot build thing or
 * post things to the ui thread.   You use the plugin context
 * to lookup resources contained specifically in the plugin.
 */
public class HelloWorldDropDownReceiver extends DropDownReceiver implements
        OnStateListener, PaneShell {

    public static final String TAG = "HelloWorldDropDownReceiver";

    public static final String SHOW_HELLO_WORLD = "com.atakmap.android.helloworld.SHOW_HELLO_WORLD";
    public static final String CHAT_HELLO_WORLD = "com.atakmap.android.helloworld.CHAT_HELLO_WORLD";
    public static final String SEND_HELLO_WORLD = "com.atakmap.android.helloworld.SEND_HELLO_WORLD";
    private final View helloView;

    private final Context pluginContext;
    private final Contact helloContact;
    private final RecyclerViewDropDown recyclerView;
    private final TabViewDropDown tabView;
    private NavigationStackDropDown navstackView;

    // Creators: every version-sensitive ATAK cluster this dropdown touches goes
    // through these seams (impls verified by the load-time systems check).
    private final LayerDownloadCreator layerDownloadCreator;

    // The Pane controller owns the pane's view wiring for every migrated
    // feature; this shell hands it the inflated pane and implements PaneShell
    // (resize/hide/map primitives) for it. Wiring still below has simply not
    // migrated yet.
    private final PaneController paneController;

    private final JoystickListener _joystickView;

    private LayerDownloadExample layerDownloader;

    private double currWidth = HALF_WIDTH;
    private double currHeight = HALF_HEIGHT;

    // DEFERRED (stream cluster): this connection-lifecycle demo — csr/cl/
    // connected, the CotStreamListener csl, _outputsChangedListener and
    // printNetworks() — is entangled with the shell's load lifecycle and has
    // not migrated yet. The addStream/removeStream buttons now live in
    // StreamController behind StreamCreator, which owns its OWN
    // CotServiceRemote connection; `connected` here is now write-only (the
    // ConnectionListener callbacks are the remaining demo).
    private final CotServiceRemote csr;
    private boolean connected = false;

    final CotServiceRemote.ConnectionListener cl = new CotServiceRemote.ConnectionListener() {
        @Override
        public void onCotServiceConnected(Bundle fullServiceState) {
            Log.d(TAG, "onCotServiceConnected: ");
            connected = true;
        }

        @Override
        public void onCotServiceDisconnected() {
            Log.d(TAG, "onCotServiceDisconnected: ");
            connected = false;
        }

    };

    final CotStreamListener csl;
    final CotServiceRemote.OutputsChangedListener _outputsChangedListener = new CotServiceRemote.OutputsChangedListener() {
        @Override
        public void onCotOutputRemoved(Bundle descBundle) {
            Log.d(TAG, "stream removed");
        }

        @Override
        public void onCotOutputUpdated(Bundle descBundle) {
            Log.v(TAG,
                    "Received ADD message for "
                            + descBundle
                                    .getString(TAKServer.DESCRIPTION_KEY)
                            + ": enabled="
                            + descBundle.getBoolean(
                                    TAKServer.ENABLED_KEY, true)
                            + ": connected="
                            + descBundle.getBoolean(
                                    TAKServer.CONNECTED_KEY, false));
        }
    };

    /**************************** CONSTRUCTOR *****************************/

    public HelloWorldDropDownReceiver(final MapView mapView,
            final Context context,
            LayerDownloadCreator layerDownloadCreator,
            PaneController paneController) {
        super(mapView);
        this.pluginContext = context;
        this.layerDownloadCreator = layerDownloadCreator;
        this.paneController = paneController;

        _joystickView = new JoystickListener();

        csr = new CotServiceRemote();
        csr.setOutputsChangedListener(_outputsChangedListener);

        csr.connect(cl);

        csl = new CotStreamListener(mapView.getContext(), TAG, null) {
            @Override
            public void onCotOutputRemoved(Bundle bundle) {
                Log.d(TAG, "stream outputremoved");
            }

            @Override
            protected void enabled(TAKServer port,
                    boolean enabled) {
                Log.d(TAG, "stream enabled");
            }

            @Override
            protected void connected(TAKServer port,
                    boolean connected) {
                Log.d(TAG, "stream connected");
            }

            @Override
            public void onCotOutputUpdated(Bundle descBundle) {
                Log.d(TAG, "stream added/updated");
            }

        };

        printNetworks();

        // If you are using a custom layout you need to make use of the PluginLayoutInflator to clear
        // out the layout cache so that the plugin can be properly unloaded and reloaded.
        helloView = PluginLayoutInflater.inflate(pluginContext,
                R.layout.hello_world_layout, null);
        // Add "Hello World" contact
        this.helloContact = addPluginContact(pluginContext.getString(
                R.string.hello_world));

        // The pane-switcher taps live in the Pane controller; it calls back
        // through PaneShell to show these sibling panes.
        recyclerView = new RecyclerViewDropDown(getMapView(), pluginContext);
        tabView = new TabViewDropDown(getMapView(), pluginContext);





        // Downloading a map layer
        final Button downloadLayer = helloView.findViewById(
                R.id.downloadMapLayer);
        downloadLayer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layerDownloader != null)
                    layerDownloader.dispose();
                layerDownloader = new LayerDownloadExample(mapView,
                        pluginContext, layerDownloadCreator);
                layerDownloader.start();
            }
        });

        // Hand the inflated pane to the Pane controller: it binds every
        // migrated listener (long-press hints, resize, pane switchers, route
        // demo) and dispatches taps to feature Controllers. This shell only
        // implements PaneShell for it.
        paneController.attach(helloView, this);
    }

    private void test(View v) {

    }

    /**************************** PUBLIC METHODS *****************************/

    @Override
    public void disposeImpl() {
        // Feature Controllers tear down their own registrations (Disposable
        // cascade); teardown still below has simply not migrated yet.
        paneController.dispose();

        // Remove Hello World contact
        removeContact(this.helloContact);

        _joystickView.dispose();

    }

    /**************************** INHERITED METHODS *****************************/

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "showing hello world drop down");

        final String action = intent.getAction();
        if (action == null)
            return;

        // Show drop-down
        switch (action) {
            case SHOW_HELLO_WORLD:
                if (!isClosed()) {
                    Log.d(TAG, "the drop down is already open");
                    unhideDropDown();
                    return;
                }

                showDropDown(helloView, HALF_WIDTH, FULL_HEIGHT,
                        FULL_WIDTH, HALF_HEIGHT, false, this);
                setAssociationKey("helloWorldPreference");
                List<Contact> allContacts = Contacts.getInstance()
                        .getAllContacts();
                for (Contact c : allContacts) {
                    if (c instanceof IndividualContact)
                        Log.d(TAG, "Contact IP address: "
                                + getIpAddress((IndividualContact) c));
                }

                break;

            // Chat message sent to Hello World contact
            case CHAT_HELLO_WORLD:
                Bundle cotMessage = intent.getBundleExtra(
                        ChatManagerMapComponent.PLUGIN_SEND_MESSAGE_EXTRA);

                String msg = cotMessage.getString("message");

                if (!FileSystemUtils.isEmpty(msg)) {
                    // Display toast to show the message was received
                    toast(helloContact.getName() + " received: " + msg);
                }
                break;

            // Sending CoT to Hello World contact
            case SEND_HELLO_WORLD:
                // Map item UID
                String uid = intent.getStringExtra("targetUID");
                MapItem mapItem = getMapView().getRootGroup().deepFindUID(uid);
                if (mapItem != null) {
                    // Display toast to show the CoT was received
                    toast(helloContact.getName() + " received request to send: "
                            + ATAKUtilities.getDisplayName(mapItem));
                }
                break;

        }
    }

    public NetConnectString getIpAddress(IndividualContact ic) {
        Connector ipConnector = ic.getConnector(IpConnector.CONNECTOR_TYPE);
        if (ipConnector != null) {
            String connectString = ipConnector.getConnectionString();
            return NetConnectString.fromString(connectString);
        } else {
            return null;
        }

    }

    @Override
    protected void onStateRequested(int state) {
        if (state == DROPDOWN_STATE_FULLSCREEN) {
            if (!isPortrait()) {
                if (Double.compare(currWidth, HALF_WIDTH) == 0) {
                    resize(FULL_WIDTH - HANDLE_THICKNESS_LANDSCAPE,
                            FULL_HEIGHT);
                }
            } else {
                if (Double.compare(currHeight, HALF_HEIGHT) == 0) {
                    resize(FULL_WIDTH, FULL_HEIGHT - HANDLE_THICKNESS_PORTRAIT);
                }
            }
        } else if (state == DROPDOWN_STATE_NORMAL) {
            if (!isPortrait()) {
                resize(HALF_WIDTH, FULL_HEIGHT);
            } else {
                resize(FULL_WIDTH, HALF_HEIGHT);
            }
        }
    }

    @Override
    public void onDropDownSelectionRemoved() {
    }

    @Override
    public void onDropDownVisible(boolean v) {
    }

    @Override
    public void onDropDownSizeChanged(double width, double height) {
        currWidth = width;
        currHeight = height;
    }

    @Override
    public void onDropDownClose() {
        // If the Map Item inspector is running, the Pane controller turns it
        // off and clears the button's toggled state (that wiring lives there
        // now; button state is View state, boundary-legal in a Controller).
        paneController.onPaneClose(helloView);
    }

    /************************* Helper Methods *************************/

    private void toast(String str) {
        Toast.makeText(getMapView().getContext(), str,
                Toast.LENGTH_LONG).show();
    }

    /* ---------------- PaneShell: the shell as the Pane controller sees it ---------------- */

    @Override
    public void resizePane(double widthFraction, double heightFraction) {
        resize(widthFraction, heightFraction);
    }

    @Override
    public void hidePane() {
        getMapView().post(() -> DropDownManager.getInstance().hidePane());
    }

    @Override
    public void retainPane() {
        setRetain(true);
    }

    @Override
    public double[] mapCenterLatLon() {
        GeoPointMetaData sp = getMapView().getPointWithElevation();
        return new double[] {
                sp.get().getLatitude(), sp.get().getLongitude()
        };
    }

    @Override
    public Context hostContext() {
        return getMapView().getContext();
    }

    @Override
    public void showRecyclerPane() {
        setRetain(true);
        recyclerView.show();
    }

    @Override
    public void showTabPane() {
        setRetain(true);
        tabView.show();
    }

    @Override
    public void pushNavigationStackPane() {
        LayoutInflater inflater = LayoutInflater.from(pluginContext);
        View layout = inflater.inflate(R.layout.navigation_stack, null);
        navstackView = new NavigationStackDropDown(getMapView(), layout,
                pluginContext);

        DropDownNavigationStack navStack = new DropDownNavigationStack(
                getMapView(),
                SEVEN_SIXTEENTH_WIDTH,
                FULL_HEIGHT,
                FULL_WIDTH,
                HALF_HEIGHT);

        navstackView.setNavigationStack(navStack);
        navStack.pushView(navstackView);
    }

    void printNetworks() {
        /*
         *    TAKServer.DESCRIPTION_KEY
         *    TAKServer.ENABLED_KEY
         *    TAKServer.CONNECTED_KEY
         *    TAKServer.CONNECT_STRING_KEY
         */
        Bundle b = CommsMapComponent.getInstance().getAllPortsBundle();
        Bundle[] streams = (Bundle[]) b.getParcelableArray("streams");
        Bundle[] outputs = (Bundle[]) b.getParcelableArray("outputs");
        Bundle[] inputs = (Bundle[]) b.getParcelableArray("inputs");
        if (inputs != null) {
            for (Bundle input : inputs)
                Log.d(TAG, "input " + input.getString(TAKServer.DESCRIPTION_KEY)
                        + ": " + input.getString(TAKServer.CONNECT_STRING_KEY));
        }
        if (outputs != null) {
            for (Bundle output : outputs)
                Log.d(TAG, "output " + output.getString(TAKServer.DESCRIPTION_KEY)
                        + ": " + output.getString(TAKServer.CONNECT_STRING_KEY));
        }
        if (streams != null) {
            for (Bundle stream : streams)
                Log.d(TAG, "stream " + stream.getString(TAKServer.DESCRIPTION_KEY)
                        + ": " + stream.getString(TAKServer.CONNECT_STRING_KEY));
        }
    }

    /**
     * For plugins to have custom radial menus, we need to set the "menu" metadata to
     * contain a well formed xml entry.   This only allows for reskinning of existing
     * radial menus with icons and actions that already exist in ATAK.
     * In order to perform a completely custom radia menu instalation. You need to
     * define the radial menu as below and then uuencode the sub elements such as
     * images or instructions.
     */
    /**
     * Add a plugin-specific contact to the contacts list
     * This contact fires an intent when a message is sent to it,
     * instead of using the default chat implementation
     *
     * @param name Contact display name
     * @return New plugin contact
     */
    public Contact addPluginContact(String name) {

        // Add handler for messages
        HelloWorldContactHandler contactHandler = new HelloWorldContactHandler(
                pluginContext);
        CotMapComponent.getInstance().getContactConnectorMgr()
                .addContactHandler(contactHandler);

        // Create new contact with name and random UID
        IndividualContact contact = new IndividualContact(
                name, UUID.randomUUID().toString());

        // Add plugin connector which points to the intent action
        // that is fired when a message is sent to this contact
        contact.addConnector(new PluginConnector(CHAT_HELLO_WORLD));

        // Add IP connector so the contact shows up when sending CoT or files
        contact.addConnector(new IpConnector(SEND_HELLO_WORLD));

        // Set default connector to plugin connector
        AtakPreferences prefs = new AtakPreferences(getMapView().getContext());
        prefs.set("contact.connector.default." + contact.getUID(),
                PluginConnector.CONNECTOR_TYPE);

        // Add new contact to master contacts list
        Contacts.getInstance().addContact(contact);

        return contact;
    }

    /**
     * Remove a contact from the master contacts list
     * This will remove it from the contacts list drop-down
     *
     * @param contact Contact object
     */
    public void removeContact(Contact contact) {
        Contacts.getInstance().removeContact(contact);
    }

}
