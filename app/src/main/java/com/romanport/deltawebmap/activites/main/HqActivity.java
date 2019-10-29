package com.romanport.deltawebmap.activites.main;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.ColorMatrixColorFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.romanport.deltawebmap.HttpTool;
import com.romanport.deltawebmap.R;
import com.romanport.deltawebmap.activites.main.dino.DinoDialogFragment;
import com.romanport.deltawebmap.entities.BannerEvent;
import com.romanport.deltawebmap.entities.DeltaVector2;
import com.romanport.deltawebmap.entities.DeltaVector3;
import com.romanport.deltawebmap.entities.api.AppConfig;
import com.romanport.deltawebmap.entities.api.DeltaUser;
import com.romanport.deltawebmap.entities.api.DeltaUserServer;
import com.romanport.deltawebmap.entities.api.GuildOfflineData;
import com.romanport.deltawebmap.entities.api.dinosaur.ArkDinoPackage;
import com.romanport.deltawebmap.entities.api.guilds.GuildCreateSession;
import com.romanport.deltawebmap.entities.api.overview.TribeOverviewPayload;
import com.romanport.deltawebmap.entities.api.tribes.TribeMapDino;
import com.romanport.deltawebmap.entities.api.tribes.TribeMapPayload;
import com.romanport.deltawebmap.entities.misc.JsMapSetup;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.LinkedList;
import java.util.List;

public class HqActivity extends AppCompatActivity implements GuildListDialogFragment.Listener, DinoDialogFragment.Listener {

    public HqTribeSearchView tribeSearchView;
    public EditText tribeSearchInput;
    public HqMapWebView map;

    public List<BannerEvent> events;

    public DeltaUser user;
    public DeltaUserServer server;
    public AppConfig config;

    public GuildCreateSession session;
    public DeltaVector3 lastMapPos;
    public TribeMapPayload tribe;
    public TribeOverviewPayload overview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hq);
        events = new LinkedList<>();

        //Get the user and server data
        Intent intent = getIntent();
        user = (DeltaUser)intent.getSerializableExtra("user-data");
        config = (AppConfig) intent.getSerializableExtra("config");
        server = (DeltaUserServer)intent.getSerializableExtra("server-data");

        //Grab important elements
        tribeSearchView = (HqTribeSearchView)findViewById(R.id.tribeSearch);
        tribeSearchInput = (EditText)findViewById(R.id.tribeSearchQuery);
        map = (HqMapWebView)findViewById(R.id.map_webview);

        //Attach event listeners
        final HqActivity hq = this;
        tribeSearchInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean hasfocus) {
                if (hasfocus) {
                    tribeSearchView.DoShow();
                    tribeSearchView.OnQueryChanged(tribeSearchInput.getText().toString(), overview, session, hq);
                }
            }
        });
        tribeSearchInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                tribeSearchView.OnQueryChanged(s.toString(), overview, session, hq);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        tribeSearchView.SetContent((RecyclerView)findViewById(R.id.tribeSearchContent), tribeSearchInput, (ImageView)findViewById(R.id.serverIcon), (ImageView)findViewById(R.id.searchBack), config);

        //Set defaults
        lastMapPos = DeltaVector3.Create(-128f, 128f, 1f);

        //Fill in parts of the UI with this server data
        AddWebImage(user.profile_image_url, (ImageView)findViewById(R.id.serverIcon));
        ((EditText)findViewById(R.id.tribeSearchQuery)).setHint("Search "+server.display_name);

        //Load data from the bundle, if possible
        if(savedInstanceState != null) {
            if(savedInstanceState.containsKey("server-s-session"))
                session = (GuildCreateSession)savedInstanceState.getSerializable("server-s-session");
            if(savedInstanceState.containsKey("server-s-map-pos"))
                lastMapPos = (DeltaVector3) savedInstanceState.getSerializable("server-s-map-pos");
            if(savedInstanceState.containsKey("server-s-tribe"))
                tribe = (TribeMapPayload) savedInstanceState.getSerializable("server-s-tribe");
            if(savedInstanceState.containsKey("server-s-overview"))
                overview = (TribeOverviewPayload) savedInstanceState.getSerializable("server-s-overview");
        }

        //Now, start or continue init. Fetch the session data if we do not already have it
        if(session == null) {
            HttpTool.SendGet(this, server.endpoint_createsession, GuildCreateSession.class, new Response.Listener<GuildCreateSession>() {
                @Override
                public void onResponse(GuildCreateSession response) {
                    session = response;
                    InitOnSessionLoaded();
                }
            });
        } else {
            InitOnSessionLoaded();
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        //Write
        if(session != null)
            state.putSerializable("server-s-session", session);
        if(lastMapPos != null)
            state.putSerializable("server-s-map-pos", lastMapPos);
        if(tribe != null)
            state.putSerializable("server-s-tribe", tribe);
        if(overview != null)
            state.putSerializable("server-s-overview", overview);
    }

    @Override
    public void onBackPressed() {
        //Check if we should catch anything before the closure of the activity
        if(tribeSearchView.isShown) {
            tribeSearchView.DoCollapse();
            return;
        }

        //Let it fall through
        super.onBackPressed();
    }

    private static final float[] NEGATIVE_IMG = {
            -1.0f,     0,     0,    0, 255, // red
            0, -1.0f,     0,    0, 255, // green
            0,     0, -1.0f,    0, 255, // blue
            0,     0,     0, 1.0f,   0  // alpha
    }; //Inverts colors. Thanks to https://stackoverflow.com/questions/17841787/invert-colors-of-drawable-android

    public static void AddWebImage(String url, ImageView v, Boolean doInvert) {
        RequestCreator p = Picasso.get().load(url);
        p.noFade();
        p.error(R.drawable.img_failed);
        p.into(v);
        if(doInvert) {
            v.setColorFilter(new ColorMatrixColorFilter(NEGATIVE_IMG));
        } else {
            v.clearColorFilter();
        }
    }

    public static void AddWebImage(String url, ImageView v) {
        AddWebImage(url, v, false);
    }

    public void OnGuildClicked(int pos) {
        //Create an intent to relaunch this activity with
        Intent ai = new Intent(this, HqActivity.class);
        ai.putExtra("user-data", user);
        ai.putExtra("server-data", user.servers.get(pos));
        startActivity(ai);

        //Write the last guild chosen
        SharedPreferences sharedPref = getSharedPreferences("com.romanport.deltawebmap.WEB", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("com.romanport.deltawebmap.LAST_SERVER", user.servers.get(pos).id);
        editor.apply();

        //End this activity
        finish();
    }

    public void ShowServerList(View v) {
        //Show the bottom sheet
        GuildListDialogFragment gl = GuildListDialogFragment.newInstance(user);
        gl.show(getSupportFragmentManager(), "guild-list");
    }

    public void CollapseSearch(View v) {
        tribeSearchView.DoCollapse();
    }

    public void GoToMapPos(DeltaVector2 pos) {
        map.JumpToPos(pos);
        tribeSearchView.DoCollapse();
    }

    @JavascriptInterface
    public void ShowToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @JavascriptInterface
    public void MapOnDinoClicked(final String url) {
        //Jank: Find this in the tribe dino list
        TribeMapDino d = null;
        for (TribeMapDino dd : tribe.dinos) {
            if(dd.apiUrl.equals(url))
                d = dd;
        }

        if(d == null)
            return;
        final TribeMapDino fd = d;

        //Show
        Log.d("awm-hq-load", "Showing async dino modal.");
        DinoDialogFragment di = DinoDialogFragment.newInstance(url, fd.adjusted_map_pos, fd.tamedName, fd.displayClassname, fd.imgUrl);
        di.show(getSupportFragmentManager(), "dino");
    }

    @JavascriptInterface
    public void MapOnUpdateSavedPos(String x, String y, String z) {
        //For some reasons, Floats were causing issues.
        lastMapPos.x = Float.parseFloat(x);
        lastMapPos.y = Float.parseFloat(y);
        lastMapPos.z = Float.parseFloat(z);
    }

    void InitOnSessionLoaded() {
        Log.d("awm-hq-load", "Loaded session data.");

        //Now, start the map initializing. Create our settings
        JsMapSetup setup = new JsMapSetup();
        setup.bg = session.mapBackgroundColor;
        setup.map = session.maps.get(0).url;
        setup.pos = lastMapPos;
        setup.mapData = session.mapData;

        //Set up map
        HqMapWebView wv = (HqMapWebView)findViewById(R.id.map_webview);
        wv.OpenMap(setup, this);

        //Fetch or use tribe data
        if(tribe == null) {
            HttpTool.SendGet(this, session.endpoint_tribes, TribeMapPayload.class, new Response.Listener<TribeMapPayload>() {
                @Override
                public void onResponse(TribeMapPayload response) {
                    tribe = response;
                    InitOnTribeLoaded();
                }
            });
        } else {
            InitOnTribeLoaded();
        }
    }

    void InitOnTribeLoaded() {
        //Now, add all of our dinos to the map
        Log.d("awm-hq-load", "Loaded tribe data.");
        map.ExecuteMapCommand(0, tribe);

        //Now, load overview
        if(overview == null) {
            HttpTool.SendGet(this, session.endpoint_tribes_overview, TribeOverviewPayload.class, new Response.Listener<TribeOverviewPayload>() {
                @Override
                public void onResponse(TribeOverviewPayload response) {
                    overview = response;
                    InitOnOverviewLoaded();
                }
            });
        } else {
            InitOnOverviewLoaded();
        }
    }

    void InitOnOverviewLoaded() {
        Log.d("awm-hq-load", "Loaded overview data.");
        tribeSearchView.DoUnlock();
    }
}
