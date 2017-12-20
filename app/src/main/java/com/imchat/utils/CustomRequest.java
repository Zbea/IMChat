package com.imchat.utils;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Description:自定义request,方便请求复杂数据结构的参数
 */
public class CustomRequest extends Request<JSONObject> {

    private final Response.Listener<JSONObject> mListener;
    private Map<String,String> params;
    private JSONObject objects;
    private Map<String,String> mHeaders;
    private Context mContext;

    public CustomRequest(Context context, String url, Response.Listener<JSONObject> listener,
                         Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mContext=context;
        mListener = listener;
        initHeader();
    }
    public CustomRequest(Context context, int method, String url, Response.Listener<JSONObject> listener,
                         Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mContext=context;
        mListener = listener;
        initHeader();
    }


    public CustomRequest(Context context, int method, String url, JSONObject jsonObject, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        this(context,method, url, listener, errorListener);
        mContext=context;
        Iterator<String> iterator=jsonObject.keys();
        params= new HashMap<>();
        while (iterator.hasNext())
        {
            String key=iterator.next().toString();
            params.put(key,jsonObject.optString(key));
        }
    }


    public CustomRequest(Context context, int method, String url, Map<String, String> params, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        this(context,method, url, listener, errorListener);
        mContext=context;
        this.params=params;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String xmlString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            JSONObject object=new JSONObject(xmlString);
            return Response.success(object, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public Map<String, String> getParams() {
        return this.params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (mHeaders==null)
            initHeader();
        return mHeaders;
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        if (mListener!=null)
            mListener.onResponse(response);
    }

    private void initHeader(){
        mHeaders=new HashMap<>();
        mHeaders.put("client", "ANDROID");
        mHeaders.put("Usertoken", (String) SharedPreferencesUtil.get(mContext,"token",""));
        mHeaders.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 1, 1.0f));
    }
}