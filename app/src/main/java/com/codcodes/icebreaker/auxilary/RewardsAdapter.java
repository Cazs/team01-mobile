package com.codcodes.icebreaker.auxilary;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Looper;
import android.util.Log;
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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class RewardsAdapter extends ArrayAdapter
{
    private Activity context;
    private ArrayList<Reward> data;
    private static LayoutInflater inflater = null;
    private Bitmap bitmap=null;
    private String TAG = "RewardsAdapter";

    public RewardsAdapter(Activity context, ArrayList<Reward> data, int resource) {
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
        TextView rwCost = (TextView) convertView.findViewById(R.id.rwValue);

        rwName.setTypeface(null, Typeface.BOLD);
        rwName.setText("\n" + data.get(position).getRwName());
        rwDescription.setText(data.get(position).getRwDescription());
        rwCost.setText(String.valueOf(data.get(position).getRwCost()));

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

        if(data.get(position).getRwCost() < data.get(position).getUsersCoins())
            claimBtn.setVisibility(View.INVISIBLE);

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
        String usr = SharedPreference.getUsername(context);
        sendRewardCode(usr, position);
    }

    private void sendRewardCode(final String username, final int position)
    {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_claim_reward);

        Thread tCodeSender = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                try
                {
                    String eventID = WritersAndReaders.readAttributeFromConfig(Config.EVENT_ID.getValue());
                    if (eventID != null)
                    {
                        final String code = data.get(position).getRwCode();
                        String rew_id = data.get(position).getRwId();
                        String response = RemoteComms.sendGetRequest("/claimReward/" + username + "/" + rew_id + "/" + eventID + "/" + code);
                        System.err.println(">>>>>>>>>>>>>>>>>>>>>Response: " + response);

                        if (response.toLowerCase().contains("success"))
                        {
                            final TextView redimLbl = (TextView)  dialog.findViewById(R.id.retriveLbl);

                            final TextView expireLbl = (TextView)  dialog.findViewById(R.id.expireLbl);

                            final TextView expireDate = (TextView)  dialog.findViewById(R.id.expireDate);

                            final TextView txtCode = (TextView)  dialog.findViewById(R.id.rwCode);

                            final ImageView qrCode = (ImageView) dialog.findViewById(R.id.rwQRcode);

                            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(code, 500);
                            try
                            {
                                 bitmap = qrCodeEncoder.encodeAsBitmap();
                            } catch (WriterException e)
                            {
                                LocalComms.logException(e);
                            }

                            if(context!=null)
                            {
                                context.runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        txtCode.setText(code);
                                        txtCode.setTypeface(null,Typeface.BOLD_ITALIC);
                                        expireLbl.setTypeface(null,Typeface.BOLD);
                                        redimLbl.setTypeface(null,Typeface.BOLD);
                                        if(bitmap!=null)
                                            qrCode.setImageBitmap(bitmap);

                                    }
                                });
                                dialog.show();
                            }
                        } else Log.d(TAG, "Rewards from remote DB are null.");
                    }
                }
                catch (SocketTimeoutException e)
                {
                    LocalComms.logException(e);
                } catch (IOException e)
                {
                    LocalComms.logException(e);
                }
            }
        });
        tCodeSender.start();
    }
}
