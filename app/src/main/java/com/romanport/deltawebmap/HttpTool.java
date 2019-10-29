package com.romanport.deltawebmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.romanport.deltawebmap.activites.LoginActivity;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class HttpTool {

    public static <T> void SendGet(final Context c, String url, final Class<T> ty, final Response.Listener<T> callback) {
        HttpTool.SendGet(c, url, ty, callback, new Response.Listener<Integer>() {
            @Override
            public void onResponse(Integer response) {
                //Failed.
                if(response == 401 || response == 402) {
                    //We're just not signed in
                    c.startActivity(new Intent(c, LoginActivity.class));
                } else if (response == -1) {
                    //No connection
                    Toast.makeText(c, R.string.sw_failed_offline, Toast.LENGTH_LONG).show();
                } else {
                    //Hmmm...
                    //Toast.makeText(c, R.string.sw_failed_status, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static <T> void SendGet(final Context c, String url, final Class<T> ty, final Response.Listener<T> callback, final Response.Listener<Integer> failCallback) {
        SendAuthenticatedRequest(c, url, callback, failCallback, ty, null, Request.Method.GET );
    }

    ///Generic send request
    static <T> void SendAuthenticatedRequest(final Context c, final String url, final Response.Listener<T> callback, final Response.Listener<Integer> failCallback, final Class<T> ty, final byte[] body, final int method) {
        //Send request
        RequestQueue queue = Volley.newRequestQueue(c);
        StringRequest stringRequest = new StringRequest(method, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Decode this as JSON

                        try {
                            GsonBuilder builder = new GsonBuilder();
                            Gson gson = builder.create();
                            T ee = gson.fromJson(response, ty);
                            callback.onResponse(ee);
                        } catch (Exception ex) {
                            LoggingTool.Log("network-failure", "Failed to deserialize JSON data. URL: "+url+" Error: "+ex.getMessage(), 4);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null) {
                    //No server connection.
                    LoggingTool.Log("network-failure", "Got no server status error. "+url, 4);
                    failCallback.onResponse(-1);
                }
                else {
                    LoggingTool.Log("network-failure", "Got no server status code "+error.networkResponse.statusCode+": "+url, 4);
                    failCallback.onResponse(error.networkResponse.statusCode);
                }
            }


        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return body;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //Set auth headers. Grab from persist storage.
                String token = c.getSharedPreferences("com.romanport.deltawebmap.WEB", Context.MODE_PRIVATE).getString("com.romanport.deltawebmap.ACCESS_TOKEN", "");

                //Run
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", "Bearer "+token);

                return params;
            }
        };

        queue.add(stringRequest);
    }
}
