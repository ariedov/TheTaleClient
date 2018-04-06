package com.dleibovych.epictale.api.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Hamster
 * @since 02.10.2014
 */
public class JournalEntry {

    public final int timestamp;
    public final String time;
    public final String text;

    public JournalEntry(final JSONObject json) throws JSONException {
        timestamp = json.getInt("timestamp");
        time = json.getString("time");
        text = json.getString("text");
    }

}
