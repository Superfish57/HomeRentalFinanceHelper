package com.suckow.homerentalfinancehelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button debugLoginLaunch;
    Context context;

    TextView emailText;
    TextView accessTokenText;
    TextView refreshTokenText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        emailText = findViewById(R.id.emailText);
        accessTokenText = findViewById(R.id.accessTokenText);
        refreshTokenText = findViewById(R.id.refreshTokenText);

        refreshPreferences();

        debugLoginLaunch = findViewById(R.id.debugLaunchLogin);
        debugLoginLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(context, LoginActivity.class);
                startActivity(signInIntent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshPreferences();
    }

    private void refreshPreferences() {
        SharedPreferences prefs = getSharedPreferences("authTokens", MODE_PRIVATE);

        if(prefs.contains("email")) {
            emailText.setText(prefs.getString("email", "Could not find email"));
            accessTokenText.setText(prefs.getString("access_token", "Could not find access token"));
            refreshTokenText.setText(prefs.getString("refresh_token", "Could not find refresh token"));
        }
    }
}
