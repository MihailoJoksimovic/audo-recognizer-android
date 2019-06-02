package com.android.mihailojoksimovic.audiorecognizer.libs;

import android.text.TextUtils;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ApiClient extends SyncHttpClient {
    public static final int DEFAULT_SOCKET_TIMEOUT = 100 * 1000;

    private static final String BASE_URL = "http://172.31.3.112:8080/";

    private static SyncHttpClient client = new ApiClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setResponseTimeout(100*1000);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void matchAgainstSamples(short[] samples) {
        StringBuilder sbStr = new StringBuilder();



        RequestParams params = new RequestParams();
        params.put("amplitudes", strJoin(samples, ","));



        client.post(getAbsoluteUrl("song/match-amplitude"), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                AsyncHttpClient.log.w(LOG_TAG, "onSuccess(int, Header[], JSONObject) was not overriden, but callback was received");
            }
        });
    }

    private static String strJoin(short[] aArr, String sSep) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = aArr.length; i < il; i++) {
            if (i > 0)
                sbStr.append(sSep);
            sbStr.append(aArr[i]);
        }
        return sbStr.toString();
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}