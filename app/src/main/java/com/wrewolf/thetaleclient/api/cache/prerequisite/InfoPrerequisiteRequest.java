package com.wrewolf.thetaleclient.api.cache.prerequisite;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.wrewolf.thetaleclient.api.dictionary.Action;
import com.wrewolf.thetaleclient.api.request.InfoRequest;
import com.wrewolf.thetaleclient.api.response.InfoResponse;
import com.wrewolf.thetaleclient.util.PreferencesManager;

import java.net.CookieManager;

import okhttp3.OkHttpClient;

/**
 * @author Hamster
 * @since 01.01.2015
 */
public class InfoPrerequisiteRequest extends PrerequisiteRequest<InfoResponse> {

    private final OkHttpClient client;
    private final CookieManager cookieManager;

    public InfoPrerequisiteRequest(OkHttpClient client, CookieManager manager, Runnable task, ErrorCallback<InfoResponse> errorCallback, Fragment fragment) {
        super(task, errorCallback, fragment);
        this.client = client;
        this.cookieManager = manager;
    }

    @Override
    protected boolean isPreExecuted() {
        boolean isPreExecuted =
                (PreferencesManager.getAccountId() != 0)
                && (!TextUtils.isEmpty(PreferencesManager.getAccountName()))
                && (PreferencesManager.getTurnDelta() > 0)
                && (PreferencesManager.getStaticContentUrl() != null);
        for(final Action action : Action.values()) {
            isPreExecuted &= PreferencesManager.getAbilityCost(action) >= 0;
        }
        return isPreExecuted;
    }

    @Override
    protected void preExecuteAndRun() {
        new InfoRequest(client, cookieManager).execute(getApiCallback());
    }
}
