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
                            //WritersAndReaders.writeAttributeToConfig(Config.EVENT_MAX_DIST.getValue(),textView.getText().toString());
                            Toast.makeText(SettingsActivity.this,"Updated distance preference.",Toast.LENGTH_LONG).show();
                        } catch (NumberFormatException e)
                        {
                            LocalComms.logException(e);
                            Toast.makeText(SettingsActivity.this,"Could not convert \""+
                                    textView.getText().toString()+"\" to a number.",Toast.LENGTH_LONG).show();
                        } /*catch (IOException e)
                        {
                            LocalComms.logException(e);
                        }*/
                    }else
                    {
                        Toast.makeText(SettingsActivity.this,"Invalid distance.",Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });
        /*EditText edtMinAge = (EditText)findViewById(R.id.edtMinAge);
        edtMinAge.setOnEditorActionListener(new TextView.OnEditorActionListener()
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
                            WritersAndReaders.writeAttributeToConfig(Config.USR_MIN_AGE.getValue(),textView.getText().toString());
                            Toast.makeText(SettingsActivity.this,"Updated min age preference.",Toast.LENGTH_LONG).show();
                        } catch (IOException e)
                        {
                            LocalComms.logException(e);
                        }
                    }else
                    {
                        Toast.makeText(SettingsActivity.this,"Invalid age.",Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });
        EditText edtMaxAge = (EditText)findViewById(R.id.edtMaxAge);
        edtMaxAge.setOnEditorActionListener(new TextView.OnEditorActionListener()
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
                            WritersAndReaders.writeAttributeToConfig(Config.USR_MAX_AGE.getValue(),textView.getText().toString());
                            Toast.makeText(SettingsActivity.this,"Updated max age preference.",Toast.LENGTH_LONG).show();
                        } catch (IOException e)
                        {
                            LocalComms.logException(e);
                        }
                    }else
                    {
                        Toast.makeText(SettingsActivity.this,"Invalid age.",Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });
        final Spinner spnGend = (Spinner) findViewById(R.id.spnGender);
        spnGend.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                String gen = "Unspecified";
                switch (i)
                {
                    case 0:
                        gen = "Male";
                        break;
                    case 1:
                        gen = "Female";
                        break;
                    default:
                        gen = "Unspecified";
                        break;
                }
                try
                {
                    WritersAndReaders.writeAttributeToConfig(Config.USR_GEND.getValue(),gen);
                    Toast.makeText(SettingsActivity.this,"Updated min age preference.",Toast.LENGTH_LONG).show();
                } catch (IOException e)
                {
                    LocalComms.logException(e);
                }
            }
        });*/
    }
}