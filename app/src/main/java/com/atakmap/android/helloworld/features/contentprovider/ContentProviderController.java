package com.atakmap.android.helloworld.features.contentprovider;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.javacodegeeks.android.contentprovidertest.BirthProvider;

/**
 * The content-provider feature's Controller. Exercises the plugin-hosted
 * {@link BirthProvider} through the stock Android {@code ContentResolver} —
 * no ATAK is involved, so there is no Creator behind this one; the Controller
 * exists for the uniform tap → Controller shape (ADR-0005).
 */
public class ContentProviderController {

    private final Context pluginContext;

    public ContentProviderController(Context pluginContext) {
        this.pluginContext = pluginContext;
    }

    /** Delete every record, then insert four demo rows, toasting each step. */
    public void exerciseBirthdayProvider() {
        // delete all the records and the table of the database provider
        String URL = "content://com.javacodegeeks.provider.Birthday/friends";
        Uri friends = Uri.parse(URL);
        int count = pluginContext.getContentResolver().delete(
                friends, null, null);
        toast("Javacodegeeks: " + count + " records are deleted.");

        String[] names = new String[] {
                "Joe", "Bob", "Sam", "Carol"
        };
        String[] dates = new String[] {
                "01/01/2001", "01/01/2002", "01/01/2003", "01/01/2004"
        };
        for (int i = 0; i < names.length; ++i) {
            ContentValues values = new ContentValues();
            values.put(BirthProvider.NAME, names[i]);
            values.put(BirthProvider.BIRTHDAY, dates[i]);
            Uri uri = pluginContext.getContentResolver().insert(
                    BirthProvider.CONTENT_URI, values);
            toast("Javacodegeeks: " + uri + " inserted!");
        }
    }

    private void toast(String msg) {
        Toast.makeText(pluginContext, msg, Toast.LENGTH_SHORT).show();
    }
}
