package com.codcodes.icebreaker;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        TextView headingTextView = (TextView) findViewById(R.id.LogIn);
        headingTextView.setTypeface(heading);

    }

    public void showMainPage(View v)
    {
        Intent mainScreen = new Intent(this,MainPageActivity.class);
        startActivity(mainScreen);
    }


}
