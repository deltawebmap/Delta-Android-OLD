package com.romanport.deltawebmap.activites;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.romanport.deltawebmap.R;
import com.romanport.deltawebmap.entities.api.AppConfig;

public class NoGuildsActivity extends AppCompatActivity {

    public AppConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_guilds);
        config = (AppConfig) getIntent().getSerializableExtra("config");
    }

    public void OnLearnMoreBtnPress(View v) {
        //Called when the user presses the login button. Open the login prompt
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(config.urls.get("LEARN_MORE_SERVER_OWNERS")));
        startActivity(browserIntent);
        finish();
    }
}
