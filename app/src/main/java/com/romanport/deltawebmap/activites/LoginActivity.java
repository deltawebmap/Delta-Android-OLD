package com.romanport.deltawebmap.activites;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.romanport.deltawebmap.R;
import com.romanport.deltawebmap.entities.api.AppConfig;

public class LoginActivity extends AppCompatActivity {

    public AppConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        config = (AppConfig) getIntent().getSerializableExtra("config");
    }

    public void OnLoginBtnPress(View v) {
        //Called when the user presses the login button. Open the login prompt
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(config.urls.get("LOGIN_ANDROID")));
        startActivity(browserIntent);
        finish();
    }

    public void OnPrivacyBtnPress(View v) {
        //Called when the user presses the login button. Open the login prompt
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(config.urls.get("PRIVACY")));
        startActivity(browserIntent);
    }
}
