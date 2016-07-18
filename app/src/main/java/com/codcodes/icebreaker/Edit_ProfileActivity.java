package com.codcodes.icebreaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.tabs.ImageConverter;

import java.io.FileNotFoundException;

public class Edit_ProfileActivity extends AppCompatActivity
{
    ImageView circularImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit__profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        Typeface h = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");
        TextView name = (TextView) toolbar.findViewById(R.id.Edit_Heading);
        name.setTypeface(h);

        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.seleena);
        Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, 100);

        circularImageView = (ImageView) findViewById(R.id.editprofilepic);
        circularImageView.setImageBitmap(circularbitmap);

        Typeface EditFont = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        TextView editphoto = (TextView) findViewById(R.id.editphoto);
        editphoto.setTypeface(EditFont);


        editphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);

            }
        });

    }
        public void onActivityResult(int requstCode,int resltCode,Intent data) {
               super.onActivityResult(requstCode,resltCode,data);
                Uri targetUri = data.getData();
                Bitmap bitmap;
                try
                {
                    bitmap=BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                    circularImageView.setImageBitmap(bitmap);
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }



    @Override
    public void onBackPressed()
    {

        super.onBackPressed();
        startActivity(new Intent(this,MainPageActivity.class));
        finish();
    }


}
