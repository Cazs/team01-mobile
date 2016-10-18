package com.codcodes.icebreaker.screens;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.LocalComms;

public class SettingsActivity extends AppCompatActivity
{
    private Typeface ttfInfinity, ttfAilerons;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ttfInfinity = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        ttfAilerons = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");

        TextView headingTextView = (TextView) findViewById(R.id.main_heading);
        headingTextView.setTypeface(ttfAilerons);
        headingTextView.setTextSize(30);

        Spinner spnGender = (Spinner)findViewById(R.id.spnGender);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gndr, R.layout.spinner);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spnGender.setAdapter(adapter);

        EditText edtDistance = (EditText)findViewById(R.id.edtDistance);
        edtDistance.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if (i== EditorInfo.IME_ACTION_DONE)
                {
                    if(textView==null)
                        return false;
                    if(!textView.getText().toString().isEmpty())
                    {
                        try
                        {
                            MainActivity.range = Double.parseDouble(textView.getText().toString());
                            Toast.makeText(SettingsActivity.this,"Updated distance preference.",Toast.LENGTH_LONG).show();
                        } catch (NumberFormatException e)
                        {
                            LocalComms.logException(e);
                            Toast.makeText(SettingsActivity.this,"Could not convert \""+
                                    textView.getText().toString()+"\" to a number.",Toast.LENGTH_LONG).show();
                        }
                    }else
                    {
                        Toast.makeText(SettingsActivity.this,"Invalid distance.",Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });
    }
}
