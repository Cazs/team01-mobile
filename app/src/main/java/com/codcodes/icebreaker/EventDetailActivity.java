package com.codcodes.icebreaker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.codcodes.icebreaker.tabs.ImageConverter;

import org.w3c.dom.Text;

public class EventDetailActivity extends AppCompatActivity {

    private String[] PeopleNames =
            {
                    "Selena Gomez",
                    "Lindsey Morgan",
                    "Liam Hemsworth",
                    "Kara Supergirl"


            };
    private String[] PeopleDescrp =
            {
                    "They call me casanova",
                    "Where Dreams come true",
                    "Party in my pants...",
                    "Fly with me"


            };
    private Integer[] imgid=
            {
                    R.drawable.seleena,
                    R.drawable.seleena,
                    R.drawable.seleena,
                    R.drawable.seleena
            };

    private ListView lv;
    private ViewFlipper vf;
    private TextView eventDetails;

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

            String imagePath = Environment.getExternalStorageDirectory().getPath().toString()
                    + extras.getString("Image ID");

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ALPHA_8;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imagePath);
            Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, 100);
            ImageView eventImage = (ImageView) findViewById((R.id.event_image));

            eventImage.setImageBitmap(circularbitmap);
            bitmap.recycle();
        }



        eventDetails = (TextView)findViewById(R.id.Event_Heading);
        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        eventDetails.setTypeface(heading);

        EditText accessCode = (EditText) findViewById(R.id.AccessCode);
        accessCode.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionID, KeyEvent event)
            {
                if (actionID== EditorInfo.IME_ACTION_DONE)
                {
                    listPeople();
                }
                return false;
            }
        });





    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), MainPageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("com.codcodes.icebreaker.Back",true);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);

    }


    private void listPeople()
    {
        //read from database
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        CustomListAdapter cla = new CustomListAdapter(this,PeopleNames,null,PeopleDescrp);
        lv= (ListView) findViewById(R.id.contactList);
        lv.setAdapter(cla);
        vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        eventDetails.setText("List Of People");
        vf.showNext();



        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                // TODO Auto-generated method stub
                String Selcteditem = PeopleNames[+position];
                String eventDescrip = PeopleDescrp[+position];
                int imageID = imgid[+position];


                Intent intent = new Intent(view.getContext(),Other_Profile.class);
                startActivity(intent);
            }
        });
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent i = new Intent(this,MainPageActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("com.codcodes.icebreaker.Back",true);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

}
