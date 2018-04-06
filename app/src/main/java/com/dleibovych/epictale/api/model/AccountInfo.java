package com.dleibovych.epictale.api.model;

import com.dleibovych.epictale.util.ObjectUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Hamster
 * @since 01.10.2014
 */
public class AccountInfo {

    public final boolean isOwnInfo;
    public final int newMessagesCount;
    public final int accountId;
    public final int lastVisitTime;
    public final boolean isInPvpQueue;
    public final boolean isObsoleteInfo;
    public final HeroInfo hero;

    public AccountInfo(final JSONObject json) throws JSONException {
        isOwnInfo = json.getBoolean("is_own");
        newMessagesCount = json.optInt("new_messages", 0);
        accountId = json.getInt("id");
        lastVisitTime = json.getInt("last_visit");
        isInPvpQueue = json.getBoolean("in_pvp_queue");
        isObsoleteInfo = json.getBoolean("is_old");
        hero = ObjectUtils.getModelFromJson(HeroInfo.class, json.getJSONObject("hero"));
    }

}
