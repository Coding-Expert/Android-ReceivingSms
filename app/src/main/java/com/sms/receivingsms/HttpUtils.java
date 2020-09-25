package com.sms.receivingsms;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class HttpUtils {

    private static final String api_url = "http://202.76.236.10/cms2";
    private static AsyncHttpClient client= new AsyncHttpClient();

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl){
        return api_url + relativeUrl;
    }
}
