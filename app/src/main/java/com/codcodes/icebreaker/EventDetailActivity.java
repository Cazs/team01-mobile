package com.codcodes.icebreaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.tabs.ImageConverter;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       // getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();



        if(extras != null)
        {
            String evtName = extras.getString("Event Name");
            TextView eventName = (TextView)findViewById(R.id.event_name);
            eventName.setText(evtName);

            TextView eventDescription = (TextView)findViewById(R.id.event_description);
            eventDescription.setText(extras.getString("Event Description"));

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),extras.getInt("Image ID") );
            Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, 100);


            ImageView eventImage = (ImageView) findViewById((R.id.event_image));

            eventImage.setImageBitmap(circularbitmap);
            bitmap.recycle();

        }

        TextView eventDetails = (TextView)findViewById(R.id.Event_Heading);
        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        eventDetails.setTypeface(heading);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), MainPageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("com.codcodes.icebreaker.Back",true);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent i = new Intent(this,MainPageActivity.class);
        i.putExtra("com.codcodes.icebreaker.Back",true);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

}
