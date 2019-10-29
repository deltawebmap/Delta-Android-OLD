package com.romanport.deltawebmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Response;
import com.romanport.deltawebmap.activites.LoginActivity;
import com.romanport.deltawebmap.activites.NoGuildsActivity;
import com.romanport.deltawebmap.activites.main.HqActivity;
import com.romanport.deltawebmap.entities.api.AppConfig;
import com.romanport.deltawebmap.entities.api.DeltaUser;
import com.romanport.deltawebmap.entities.api.DeltaUserServer;
import com.romanport.deltawebmap.entities.api.LoginPreflightResponse;

public class StartupActivity extends AppCompatActivity {

    /* CONFIG; MOVE THIS LATER */

    public String enviornment = "prod";
    public Integer version = 0;

    /* END CONFIG */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //This could be a request to log in. Grab the preflight token if it was sent
        Intent intent = getIntent();
        if(intent.getCategories().contains("android.intent.category.BROWSABLE")) {
            //This is a preflight request
            Uri data = intent.getData();
            String preflightToken = data.getPath().replace("/","");
            RunLoginReturn(preflightToken);
        } else {
            //This is a normal startup
            Run();
        }
    }

    void RunLoginReturn(String token) {
        //Verify the preflight token we got from login.
        HttpTool.SendGet(this, "https://deltamap.net/api/auth/validate_preflight_token?id="+token, LoginPreflightResponse.class, new Response.Listener<LoginPreflightResponse>() {
            @Override
            public void onResponse(LoginPreflightResponse r) {
                //OK!
                LoggingTool.Log("login", "Got full token. Setting it as our access token and requesting user data.", 2);

                //Store token
                SharedPreferences sharedPref = getSharedPreferences("com.romanport.deltawebmap.WEB", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("com.romanport.deltawebmap.ACCESS_TOKEN", r.token);
                editor.commit();

                //Show welcome message
                Toast toast = Toast.makeText(getApplicationContext(), "Hey there, "+r.user.screen_name+"!", Toast.LENGTH_LONG);
                toast.show();

                //Start the main view
                Run();
            }
        },new Response.Listener<Integer>() {
            @Override
            public void onResponse(Integer r) {
                //Failed
                LoggingTool.Log("login-error", "Login failed.", 5);
                Toast toast = Toast.makeText(getApplicationContext(), R.string.login_return_failed, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    void Run() {
        //Request the config
        final Activity c = this;
        HttpTool.SendGet(c, "https://config.deltamap.net/"+enviornment+"/app_config.json?v=0&p=android", AppConfig.class, new Response.Listener<AppConfig>() {
            @Override
            public void onResponse(AppConfig response) {
                //Check if the system is even up
                if(!response.status) {
                    //Not up.
                    AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.HqTheme_alertDialog);
                    builder.setMessage(response.status_message).setTitle(R.string.startup_status_title);
                    builder.setPositiveButton(R.string.startup_offline_retry, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Run();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return;
                }

                //Check if we're within version requirements
                if(response.oldest_versions.get("ANDROID") > version) {
                    //Not up.
                    AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.HqTheme_alertDialog);
                    builder.setMessage(R.string.old_version_sub).setTitle(R.string.old_version_title);
                    builder.setPositiveButton(R.string.startup_offline_retry, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Run();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return;
                }

                //All good. Authenticate
                AuthAndRun(c, response);
            }
        }, new Response.Listener<Integer>() {
            @Override
            public void onResponse(Integer response) {
                //We'll notify the user that something is wrong
                AlertOffline();
            }
        });

    }

    void AuthAndRun(final Activity c, final AppConfig config) {
        //Request user data
        HttpTool.SendGet(this, config.api+"/users/@me", DeltaUser.class, new Response.Listener<DeltaUser>() {
            @Override
            public void onResponse(DeltaUser response) {
                //Start the service
                LaunchService(response, config);
            }
        }, new Response.Listener<Integer>() {
            @Override
            public void onResponse(Integer response) {
                //Failed.
                if (response == 401 || response == 402) {
                    //We're just not signed in
                    Intent i = new Intent(c, LoginActivity.class);
                    i.putExtra("config", config);
                    c.startActivity(i);
                    c.finish();
                } else {
                    AlertOffline();
                }
            }
        });
    }

    void AlertOffline() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.HqTheme_alertDialog);
        builder.setMessage(R.string.startup_offline_message).setTitle(R.string.startup_offline_title);
        builder.setPositiveButton(R.string.startup_offline_retry, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Run();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void LaunchService(DeltaUser u, AppConfig config) {
        //Check if we have any servers
        if(u.servers.size() == 0) {
            //No servers!
            Intent i = new Intent(this, NoGuildsActivity.class);
            i.putExtra("config", config);
            startActivity(i);
            finish();
            return;
        }

        //Check to see if we can find the last server used
        String lastServerId = getSharedPreferences("com.romanport.deltawebmap.WEB", Context.MODE_PRIVATE).getString("com.romanport.deltawebmap.LAST_SERVER", null);
        DeltaUserServer lastServer = u.servers.get(0);
        for (DeltaUserServer s: u.servers) {
            if(s.id.equals(lastServerId) && !s.is_hidden)
                lastServer = s;
        }

        //Start the application
        Intent ai = new Intent(this, HqActivity.class);
        ai.putExtra("user-data", u);
        ai.putExtra("server-data", lastServer);
        ai.putExtra("config", config);
        startActivity(ai);

        //Finish this activity
        finish();
    }
}
