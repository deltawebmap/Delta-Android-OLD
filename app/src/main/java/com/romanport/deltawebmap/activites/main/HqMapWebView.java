package com.romanport.deltawebmap.activites.main;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.romanport.deltawebmap.entities.DeltaVector2;
import com.romanport.deltawebmap.entities.misc.JsMapSetup;

import java.util.LinkedList;
import java.util.List;

public class HqMapWebView extends WebView {

    public HqMapWebView(Context c, AttributeSet attrs) {
        super(c, attrs);
        isReady = false;
        queuedCommands = new LinkedList<>();
    }

    public Boolean isReady;
    public HqActivity hq;
    private List<Object[]> queuedCommands;

    public void OpenMap(JsMapSetup settings, HqActivity activity) {
        //Opens a map session on a specific map URL
        hq = activity;

        //Set up first
        getSettings().setJavaScriptEnabled(true);
        addJavascriptInterface(activity, "native");
        addJavascriptInterface(this, "native_engine");
        loadUrl("file:///android_asset/map/map.html#"+GetSafeString(settings));
    }

    public void ExecuteMapCommand(final Integer opcode, final Object command) {
        //Check if we are ready
        if(!isReady) {
            //Log
            Log.d("awm-map", "Adding message with opcode "+opcode+" to queue; the map is not yet ready.");

            //Add to queue
            queuedCommands.add(new Object[] {
                    opcode,
                    command
            });
            return;
        }

        //Log
        Log.d("awm-map", "Sending message with opcode "+opcode+".");

        //Encode
        final String base64Content = GetSafeString(command);

        //Run
        post(new Runnable() {
            @Override
            public void run() {
                //Log
                Log.d("awm-map", "Executing message with opcode "+opcode+".");

                //Now, execute command
                evaluateJavascript("map.onCmd("+opcode+", '"+base64Content+"');", null);
            }
        });
    }

    public void JumpToPos(DeltaVector2 pos) {
        ExecuteMapCommand(1, pos);
    }

    private String GetSafeString(Object o) {
        //Encode using JSON first
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String content = gson.toJson(o);

        //Now, base64 encode. Gross.
        return Base64.encodeToString(content.getBytes(), Base64.NO_PADDING|Base64.NO_WRAP|Base64.CRLF);
    }

    @JavascriptInterface
    public void JsIsPrepared() {
        Log.d("awm-map", "Loaded.");
        //Called from the map JS. Tells us that we are ready. Send any queued commands
        isReady = true;
        for (Object[] cmd: queuedCommands) {
            ExecuteMapCommand((Integer)cmd[0], cmd[1]);
        }
        queuedCommands.clear();

        //Show
        hq.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                setVisibility(VISIBLE);
            }
        });
    }
}
