package com.codcodes.icebreaker;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.tabs.ImageConverter;

/**
 * Created by USER on 2016/07/20.
 */
public class CustomListAdapter extends ArrayAdapter<String>
{
    private final Activity context;
<<<<<<< HEAD
=======

>>>>>>> e4f08746661cde9588c6e48ed983400256080138
    private final String[] eventNames;
    private final String[] eventIcons;
    private final String[] eventDescriptions;
    private static AssetManager mgr;

    public CustomListAdapter(Activity context, String[] eventNames, String[] eventIcons, String[] eventDescriptions)
    {
        super(context, R.layout.customlist, eventNames);
        this.context=context;
        this.eventNames=eventNames;
        this.eventIcons=eventIcons;
        this.eventDescriptions =eventDescriptions;
<<<<<<< HEAD
=======

>>>>>>> e4f08746661cde9588c6e48ed983400256080138
    }

    public View getView(int position, View view, ViewGroup parent)
    {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.customlist, null,true);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;

<<<<<<< HEAD
        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                + eventIcons[position], options);
        //selected_photo.setImageBitmap(bitmap);

        //Bitmap bitmap = ImageUtils.getInstant().compressBitmapImage(rowView.getResources(),eventIcons[position]);
=======
      //  Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
              //  + eventIcons[position], options);
        //selected_photo.setImageBitmap(bitmap);

        Bitmap bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                + eventIcons[position],context);
>>>>>>> e4f08746661cde9588c6e48ed983400256080138
        Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);

       // Typeface h = Typeface.createFromAsset(mgr,"Infinity.ttf");

        TextView txtTitle = (TextView) rowView.findViewById(R.id.event_title);
        //txtTitle.setTypeface(h);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.event_icon);

        TextView extratxt = (TextView) rowView.findViewById(R.id.event_description);
        //extratxt.setTypeface(h);

        txtTitle.setText(eventNames[position]);
        imageView.setImageBitmap(circularbitmap);
        extratxt.setText(eventDescriptions[position]);

        txtTitle.setTextColor(Color.BLACK);
        bitmap.recycle();
        return rowView;

    }
}
