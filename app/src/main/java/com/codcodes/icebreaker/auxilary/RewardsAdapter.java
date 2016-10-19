package com.codcodes.icebreaker.auxilary;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.Reward;
import com.google.zxing.*;

import java.util.ArrayList;

public class RewardsAdapter extends ArrayAdapter
{
    private Context context;
    private ArrayList<Reward> data;
    private static LayoutInflater inflater = null;

    public RewardsAdapter(Context context, ArrayList<Reward> data, int resource) {
        super(context, resource);
        this.data = data;
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount()
    {
        return data.size();
    }

    @Override
    public Reward getItem(int position)
    {
        return data.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        if(convertView==null)
            convertView = inflater.inflate(R.layout.rw_list_row_item, parent, false);//inflater.inflate(R.layout.ach_list_row_item,null);
        TextView rwName = (TextView) convertView.findViewById(R.id.rwName);
        TextView rwDescription = (TextView) convertView.findViewById(R.id.rwDescription);
        final Button claimBtn = (Button)  convertView.findViewById(R.id.btnClaim);

        rwName.setTypeface(null, Typeface.BOLD);
        rwName.setText("\n" + data.get(position).getRwName());
        rwDescription.setText(data.get(position).getRwDescription());

        ImageView imgAch = (ImageView)convertView.findViewById(R.id.imgRw);

        if(position==0)
            imgAch.setImageResource(R.drawable.rw);
        else if(position==3)
            imgAch.setImageResource(R.drawable.drinkic);
        else if(position==4)
            imgAch.setImageResource(R.drawable.hamper);
        else if(position==2)
            imgAch.setImageResource(R.drawable.vip);
        else
            imgAch.setImageResource(R.drawable.trophy);

       claimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rwClaimWindow(position);
            }
        });
        return convertView;
    }
    private void rwClaimWindow(int position)
    {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_claim_reward);
        TextView redimLbl = (TextView)  dialog.findViewById(R.id.retriveLbl);
        redimLbl.setTypeface(null,Typeface.BOLD);

        TextView expireLbl = (TextView)  dialog.findViewById(R.id.expireLbl);
        expireLbl.setTypeface(null,Typeface.BOLD);

        TextView expireDate = (TextView)  dialog.findViewById(R.id.expireDate);
        TextView code = (TextView)  dialog.findViewById(R.id.rwCode);
        code.setTypeface(null,Typeface.BOLD_ITALIC);
        code.setText(data.get(position).getRwCode());

        ImageView qrCode = (ImageView) dialog.findViewById(R.id.rwQRcode);
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(data.get(position).getRwCode(), 500);

        try
        {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            qrCode.setImageBitmap(bitmap);
        } catch (WriterException e)
        {
            LocalComms.logException(e);
        }
        dialog.show();
    }
}
