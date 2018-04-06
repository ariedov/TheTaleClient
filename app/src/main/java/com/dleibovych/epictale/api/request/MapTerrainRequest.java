package com.dleibovych.epictale.api.request;

import com.dleibovych.epictale.api.CommonRequest;
import com.dleibovych.epictale.api.CommonResponseCallback;
import com.dleibovych.epictale.api.HttpMethod;
import com.dleibovych.epictale.api.response.MapTerrainResponse;
import com.dleibovych.epictale.util.RequestUtils;

import org.json.JSONException;

/**
 * @author Hamster
 * @since 18.10.2014
 */
public class MapTerrainRequest extends CommonRequest {

    public static final String URL_BASE = "http://lonebytesoft.com:15934/";
    private static final String URL = URL_BASE + "the-tale/map_data.js";

    public void execute(final CommonResponseCallback<MapTerrainResponse, String> callback) {
        execute(URL, HttpMethod.GET, null, null, new CommonResponseCallback<String, Throwable>() {
            @Override
            public void processResponse(String response) {
                try {
                    RequestUtils.processResultInMainThread(callback, false, new MapTerrainResponse(response), null);
                } catch (JSONException e) {
                    RequestUtils.processResultInMainThread(callback, true, null, e.getLocalizedMessage());
                }
            }

            @Override
            public void processError(Throwable error) {
                RequestUtils.processResultInMainThread(callback, true, null, error.getLocalizedMessage());
            }
        });
    }

    @Override
    protected long getStaleTime() {
        return 60000;
    }

}
