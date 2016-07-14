package com.codcodes.icebreaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.tabs.ImageConverter;

public class Edit_ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit__profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        Typeface h = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        TextView name = (TextView) toolbar.findViewById(R.id.Edit_Heading);
        name.setTypeface(h);

        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.seleena);
        Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap,100);

        ImageView circularImageView = (ImageView) findViewById(R.id.editprofilepic);
        circularImageView.setImageBitmap(circularbitmap);

    }

}
