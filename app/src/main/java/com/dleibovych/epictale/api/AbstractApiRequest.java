package com.dleibovych.epictale.api;

import android.os.Handler;
import android.os.Looper;

import com.dleibovych.epictale.BuildConfig;
import com.dleibovych.epictale.TheTaleApplication;
import com.dleibovych.epictale.api.cache.Request;
import com.dleibovych.epictale.api.cache.RequestCacheManager;
import com.dleibovych.epictale.util.PreferencesManager;
import com.dleibovych.epictale.util.RequestUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * @author Hamster
 * @author WreWolf
 * @since 15.05.2017
 */
public abstract class AbstractApiRequest<T extends AbstractApiResponse> {

    private static final String URL = "https://the-tale.org/%s";
    private static final long RETRY_TIMEOUT_MILLIS = 1000; // 1 s

    private static final String COOKIE_CSRF_TOKEN = "csrftoken";
    private static final String PARAM_CSRF_TOKEN = "csrfmiddlewaretoken";
    private static final String PARAM_API_VERSION = "api_version";
    private static final String PARAM_CLIENT_ID = "api_client";

    private static final Handler handler = new Handler(Looper.getMainLooper());

    private final OkHttpClient httpClient;
    private final CookieManager cookieManager;
    private final HttpMethod httpMethod;
    private final String methodUrl;
    private final String version;
    private final boolean needAdditionalParams;

    protected AbstractApiRequest(final OkHttpClient httpClient, CookieManager cookieManager, final HttpMethod httpMethod, final String methodUrl, final String version, final boolean needAdditionalParams) {
        this.httpClient = httpClient;
        this.cookieManager = cookieManager;
        this.httpMethod = httpMethod;
        this.version = version;
        this.needAdditionalParams = needAdditionalParams;
        if (methodUrl.startsWith("/")) {
            this.methodUrl = methodUrl.substring(1);
        } else {
            this.methodUrl = methodUrl;
        }
    }

    protected void execute(final Map<String, String> getParams, final Map<String, String> postParams, final ApiResponseCallback<T> callback) {
        execute(getParams, postParams, callback, true);
    }

    protected void execute(final Map<String, String> getParams, final Map<String, String> postParams, final ApiResponseCallback<T> callback, final boolean useCache) {
        final String url = String.format(URL, methodUrl);
        final com.dleibovych.epictale.api.cache.Request request =
                new com.dleibovych.epictale.api.cache.Request(url,
                        httpMethod.getHttpRequest(url, getParams, postParams).getRequest(),
                        getParams,
                        postParams);

        final long staleTime = getStaleTime();
        if (useCache && (staleTime > 0) && !RequestCacheManager.initRequest(request, staleTime)) {
            RequestCacheManager.addListener(request, new CommonResponseCallback<String, Void>() {
                @Override
                public void processResponse(String response) {
                    try {
                        final T responseObject = getResponse(response);
                        processFinishedResponse(responseObject, callback);
                    } catch (JSONException e) {
                        RequestUtils.processResultInMainThread(callback, true, null, null);
                    }
                }

                @Override
                public void processError(Void error) {
                    AbstractApiRequest.this.execute(getParams, postParams, callback);
                }
            }, staleTime);
            return;
        }


        Map<String, String> requestGetParams = getParams;
        Map<String, String> requestPostParams = postParams;

        if (needAdditionalParams) {
            if (requestGetParams == null) {
                requestGetParams = new HashMap<>();
            }
            requestGetParams.put(PARAM_API_VERSION, version);
            requestGetParams.put(PARAM_CLIENT_ID,
                    String.format("%s%s%s", TheTaleApplication.Companion.getContext().getPackageName(), "-", BuildConfig.VERSION_CODE));
        }

        for (final HttpCookie httpCookie : cookieManager.getCookieStore().getCookies()) {
            if ((httpMethod == HttpMethod.POST) && (httpCookie.getName().equals(COOKIE_CSRF_TOKEN))) {
                if (requestPostParams == null) {
                    requestPostParams = new HashMap<>();
                }
                requestPostParams.put(PARAM_CSRF_TOKEN, httpCookie.getValue());
            }
        }

        final Request httpRequest = httpMethod.getHttpRequest(url, requestGetParams, requestPostParams);

        httpClient.newCall(httpRequest.getRequest()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                RequestCacheManager.onRequestFinishError(request);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                for (final HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
                    final HttpCookie httpCookie = new HttpCookie(cookie.getName(), cookie.getValue());
                    httpCookie.setDomain(cookie.getDomain());
                    httpCookie.setPath(cookie.getPath());
                    cookieManager.getCookieStore().add(URI.create(String.format("%s%s", cookie.getDomain(), cookie.getPath())), httpCookie);
                    if (cookie.getName().equals(RequestUtils.COOKIE_SESSION_ID)) {
                        PreferencesManager.setSession(cookie.getValue());
                    }
                }
                String result = response.body().string();
                if (staleTime > 0) {
                    RequestCacheManager.onRequestFinished(request, result);
                }

                if (callback != null) {
                    final String responseString;
                    responseString = result;

                    try {
                        final T responseT = getResponse(responseString);
                        if (isFinished(responseT)) {
                            processFinishedResponse(responseT, callback);
                        } else {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    retry(getParams, postParams, responseT, callback);
                                }
                            }, RETRY_TIMEOUT_MILLIS);
                        }
                    } catch (JSONException e) {
                        RequestUtils.processResultInMainThread(callback, true, null, null);
                    }
                }
            }
        });

    }

    protected abstract T getResponse(final String response) throws JSONException;

    protected void processFinishedResponse(final T response, final ApiResponseCallback<T> callback) {
        if (isError(response)) {
            RequestUtils.processResultInMainThread(callback, true, null, response);
        } else {
            RequestUtils.processResultInMainThread(callback, false, response, null);
        }
    }

    protected boolean isFinished(final T response) {
        return (response == null) || (response.status != ApiResponseStatus.PROCESSING);
    }

    protected boolean isError(final T response) {
        return (response != null)
                && ((response.status == ApiResponseStatus.ERROR) || (response.status == ApiResponseStatus.GENERIC));
    }

    protected void retry(final Map<String, String> getParams, final Map<String, String> postParams, final T response, final ApiResponseCallback<T> callback) {
        execute(getParams, postParams, callback);
    }

    protected OkHttpClient getHttpClient() {
        return httpClient;
    }

    protected CookieManager getCookieManager() {
        return cookieManager;
    }

    protected abstract long getStaleTime();

}
