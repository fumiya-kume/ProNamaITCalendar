package jp.ryohlan.mascotitstudycalendar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyRequestBuilder {
    private static final DefaultRetryPolicy CUSTOM_POLICY
            = new DefaultRetryPolicy(
            10000,
            0,
            0);

    static public StringRequest create(
            int requestMethod, String url, Response.Listener<String> completeListener,
            Response.ErrorListener errorListener, final Map params) {
        return create(requestMethod, url, completeListener, errorListener, params, "");
    }

    static public StringRequest create(
            int requestMethod, String url, Response.Listener<String> completeListener,
            Response.ErrorListener errorListener) {
        return create(requestMethod, url, completeListener, errorListener, new HashMap());
    }

    static public StringRequest create(
            int requestMethod, String url, Response.Listener<String> completeListener,
            Response.ErrorListener errorListener, final Map params, final String accessToken) {
        StringRequest request = new StringRequest(
                requestMethod,
                url,
                completeListener,
                errorListener
        ) {
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                Map<String, String> newHeaders = new HashMap<String, String>();
                newHeaders.putAll(headers);
                return newHeaders;
            }
        };
        request.setRetryPolicy(CUSTOM_POLICY);
        return request;
    }

    static public JsonObjectRequest create(
            int requestMethod, String url, Response.Listener<JSONObject> completeListener,
            Response.ErrorListener errorListener, final JSONObject jsonRequest, final String accessToken) {
        JsonObjectRequest request = new JsonObjectRequest(
                requestMethod,
                url,
                jsonRequest,
                completeListener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                Map<String, String> newHeaders = new HashMap<String, String>();
                newHeaders.putAll(headers);
                return newHeaders;
            }
        };
        request.setRetryPolicy(CUSTOM_POLICY);
        return request;
    }
}
