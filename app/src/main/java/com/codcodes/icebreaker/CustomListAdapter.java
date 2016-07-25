package com.codcodes.icebreaker;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
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
    private final String[] itemname;
    private final Integer[] imgid;
    private final String[] itemdescrip;

    private static AssetManager mgr;

    public CustomListAdapter(Activity context, String[] itemname, Integer[] imgid, String[] itemdescrip) {
        super(context, R.layout.customlist, itemname);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemname=itemname;
        this.imgid=imgid;
        this.itemdescrip =itemdescrip;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.customlist, null,true);

        Bitmap bitmap = BitmapFactory.decodeResource(rowView.getResources(), imgid[position]);
        Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);

       // Typeface h = Typeface.createFromAsset(mgr,"Infinity.ttf");

        TextView txtTitle = (TextView) rowView.findViewById(R.id.event_title);
        //txtTitle.setTypeface(h);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.event_icon);

        TextView extratxt = (TextView) rowView.findViewById(R.id.event_description);
        //extratxt.setTypeface(h);

        txtTitle.setText(itemname[position]);
        imageView.setImageBitmap(circularbitmap);
        extratxt.setText(itemdescrip[position]);

        txtTitle.setTextColor(Color.BLACK);
        bitmap.recycle();
        return rowView;

    };
}
