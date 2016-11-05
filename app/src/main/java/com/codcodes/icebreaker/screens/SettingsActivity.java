package com.codcodes.icebreaker.screens;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.Config;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity
{
    private Typeface ttfInfinity, ttfAilerons;
    private EditText edtDistance, edtMinAge, edtMaxAge, edtLoudness;
    private Spinner gender;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ttfInfinity = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        ttfAilerons = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");

        TextView headingTextView = (TextView) findViewById(R.id.main_heading);
        headingTextView.setTypeface(ttfAilerons);
        headingTextView.setTextSize(40);

        Spinner spnGender = (Spinner)findViewById(R.id.spnGender);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gndr, R.layout.spinner);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spnGender.setAdapter(adapter);

        edtDistance = (EditText)findViewById(R.id.edtDistance);
        edtMinAge = (EditText)findViewById(R.id.edtMinAge);
        edtMaxAge = (EditText)findViewById(R.id.edtMaxAge);
        edtLoudness = (EditText)findViewById(R.id.edtLoudness);
        gender = (Spinner) findViewById(R.id.spnGender);

        edtDistance.setText(String.valueOf(MainActivity.range));
        edtMinAge.setText(String.valueOf(MainActivity.min_age));
        edtMaxAge.setText(String.valueOf(MainActivity.max_age));
        edtLoudness.setText(String.valueOf(MainActivity.loudness));
        gender.setSelection(MainActivity.pref_gender<3?MainActivity.pref_gender:2);
    }

    public void updateFilters(View view)
    {
        String str_km = edtDistance.getText().toString();
        String str_min_age = edtMinAge.getText().toString();
        String str_max_age = edtMaxAge.getText().toString();
        String str_loudness = edtLoudness.getText().toString();
        int gend = gender.getSelectedItemPosition();

        if(str_km.isEmpty())
        {
            Toast.makeText(SettingsActivity.this, "Invalid distance.", Toast.LENGTH_LONG).show();
            return;
        }
        if(str_min_age.isEmpty())
        {
            Toast.makeText(SettingsActivity.this, "Invalid minimum age.", Toast.LENGTH_LONG).show();
            return;
        }
        if(str_max_age.isEmpty())
        {
            Toast.makeText(SettingsActivity.this, "Invalid maximum age.", Toast.LENGTH_LONG).show();
            return;
        }
        if(str_loudness.isEmpty())
        {
            Toast.makeText(SettingsActivity.this, "Invalid loudness.", Toast.LENGTH_LONG).show();
            return;
        }
        //else proceed as normal
        try
        {
            WritersAndReaders.writeAttributeToConfig(Config.EVENT_MAX_DIST.getValue(),str_km);
            WritersAndReaders.writeAttributeToConfig(Config.USR_MIN_AGE.getValue(),str_min_age);
            WritersAndReaders.writeAttributeToConfig(Config.USR_MAX_AGE.getValue(),str_max_age);
            WritersAndReaders.writeAttributeToConfig(Config.USR_GEND.getValue(),String.valueOf(gend));
            WritersAndReaders.writeAttributeToConfig(Config.EVENT_LOUDNESS.getValue(),str_loudness);

            MainActivity.range = Double.parseDouble(str_km);
            MainActivity.min_age = Integer.parseInt(str_min_age);
            MainActivity.max_age = Integer.parseInt(str_max_age);
            MainActivity.pref_gender = gend;
            MainActivity.loudness = Double.parseDouble(str_loudness);

            Toast.makeText(SettingsActivity.this,"Updated search preferences.",Toast.LENGTH_LONG).show();
        } catch (NumberFormatException e)
        {
            LocalComms.logException(e);
            Toast.makeText(SettingsActivity.this, "Could not convert one of your entries to a number.", Toast.LENGTH_LONG).show();
        } catch (IOException e)
        {
            Toast.makeText(SettingsActivity.this, "I/O Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            LocalComms.logException(e);
        }
    }

    public void showIcebreakHistory(View view)
    {

    }
}