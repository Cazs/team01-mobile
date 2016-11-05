package com.codcodes.icebreaker.auxilary;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.Reward;
import com.codcodes.icebreaker.model.User;
import com.google.zxing.WriterException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * Created by Casper on 2016/10/24.
 */
public class RewardsRecyclerViewAdapter extends RecyclerView.Adapter<RewardsRecyclerViewAdapter.RewardViewHolder>
{

    /**
     * {@link RecyclerView.Adapter} that can display a {@link User} and makes a call to the
     * specified {@link IOnListFragmentInteractionListener}.
     */
    private final List<Reward> mValues;
    private final List<Bitmap> mIconBitmaps;
    private final String TAG = "IB/RewRcyclrViewAdapter";
    private Bitmap bitmap;
    private Activity context;

    private final IOnListFragmentInteractionListener mListener;

    public RewardsRecyclerViewAdapter(List<Reward> items, List<Bitmap> mIconBitmaps, IOnListFragmentInteractionListener listener, Activity context)
    {
        mValues = items;
        mListener = listener;
        this.mIconBitmaps = mIconBitmaps;
        this.context=context;
    }

    @Override
    public RewardViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rw_list_row_item, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RewardViewHolder holder, int position)
    {
        holder.setReward(mValues.get(position));
        //holder.mContactName.setText(mValues.get(position).getFirstname() + " " + mValues.get(position).getLastname());
        //holder.mContactBio.setText(mValues.get(position).getCatchphrase());
        if(mIconBitmaps!=null)
        {
            if (position < mIconBitmaps.size())
                holder.getRewardIcon().setImageBitmap(mIconBitmaps.get(position));
            else
                Log.d(TAG, "Bitmap ArrayList is empty.");
        }

        holder.getView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mListener)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.getReward());
                }
            }
        });
    }

    private void showClaimWindow(final Reward reward, Dialog dialog)
    {
        String usr = SharedPreference.getUsername(context);
        sendRewardCode(usr, reward, dialog);
    }

    private void sendRewardCode(final String username, final Reward reward, final Dialog dialog)
    {
        /*final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_claim_reward);
        dialog.show();*/

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
                        if(!eventID.equals("0"))
                        {
                            final String code = WritersAndReaders.getRandomIdStr(4);
                            String rew_id = reward.getRwId();
                            String response = RemoteComms.sendGetRequest("claimReward/" + username + "/" + rew_id + "/" + eventID + "/" + code);

                            if (response.toLowerCase().contains("success"))
                            {
                                final TextView redimLbl = (TextView) dialog.findViewById(R.id.retriveLbl);

                                final TextView expireLbl = (TextView) dialog.findViewById(R.id.expireLbl);

                                final TextView expireDate = (TextView) dialog.findViewById(R.id.expireDate);

                                final TextView txtCode = (TextView) dialog.findViewById(R.id.rwCode);

                                final ImageView qrCode = (ImageView) dialog.findViewById(R.id.rwQRcode);

                                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(code, 500);
                                try
                                {
                                    bitmap = qrCodeEncoder.encodeAsBitmap();
                                } catch (WriterException e)
                                {
                                    LocalComms.logException(e);
                                }

                                if (context != null)
                                {
                                    context.runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            txtCode.setText(code);
                                            txtCode.setTypeface(null, Typeface.BOLD_ITALIC);
                                            expireLbl.setTypeface(null, Typeface.BOLD);
                                            redimLbl.setTypeface(null, Typeface.BOLD);
                                            if (bitmap != null)
                                                qrCode.setImageBitmap(bitmap);

                                        }
                                    });
                                }
                            } else
                            {
                                Log.d(TAG, response);
                                Toast.makeText(context, response, Toast.LENGTH_LONG);
                            }
                        }else
                        {
                            Toast.makeText(context, "You are not signed into any event.", Toast.LENGTH_LONG);
                        }
                    }else
                    {
                        Toast.makeText(context,"You are not signed into any event.",Toast.LENGTH_LONG);
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

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class RewardViewHolder extends RecyclerView.ViewHolder
    {
        private final View view;
        private final TextView mRewardName;
        private final TextView mRewardDescription;
        private final ImageView mRewardIcon;
        private final TextView mValue;
        private final Button btnClaim;
        private Reward reward;

        public RewardViewHolder(View view)
        {
            super(view);
            this.view = view;
            this.mRewardName = (TextView) view.findViewById(R.id.rwName);
            this.mRewardDescription = (TextView) view.findViewById(R.id.rwDescription);
            this.mRewardIcon = (ImageView) view.findViewById(R.id.imgRw);
            this.mValue = (TextView) view.findViewById(R.id.rwValue);
            this.btnClaim = (Button) view.findViewById(R.id.btnClaim);

            btnClaim.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //rwClaimWindow(reward);
                    //final Dialog dialog = new Dialog(view.getContext());
                    //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    //dialog.setContentView(R.layout.activity_claim_reward);
                    //dialog.show();
                    final Dialog d = new Dialog(context);
                    d.setContentView(R.layout.activity_claim_reward);
                    d.setCanceledOnTouchOutside(false);
                    d.setCancelable(false);
                    d.show();
                    final String username = SharedPreference.getUsername(context);
                    //showClaimWindow(reward,d);
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
                                    if(!eventID.equals("0"))
                                    {
                                        final String code = WritersAndReaders.getRandomIdStr(4);
                                        String rew_id = reward.getRwId();
                                        String response = RemoteComms.sendGetRequest("claimReward/" + username + "/" + rew_id + "/" + eventID + "/" + code);

                                        if (response.toLowerCase().contains("success"))
                                        {
                                            final TextView redimLbl = (TextView) d.findViewById(R.id.retriveLbl);

                                            final TextView expireLbl = (TextView) d.findViewById(R.id.expireLbl);

                                            final TextView expireDate = (TextView) d.findViewById(R.id.expireDate);

                                            final TextView txtCode = (TextView) d.findViewById(R.id.rwCode);

                                            final ImageView qrCode = (ImageView) d.findViewById(R.id.rwQRcode);

                                            final ProgressBar pb = (ProgressBar) d.findViewById(R.id.pb_code_load);

                                            final Button btnDismiss = (Button) d.findViewById(R.id.btnDismiss);

                                            btnDismiss.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view)
                                                {
                                                    d.dismiss();
                                                }
                                            });

                                            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(code, 500);
                                            try
                                            {
                                                bitmap = qrCodeEncoder.encodeAsBitmap();
                                            } catch (WriterException e)
                                            {
                                                LocalComms.logException(e);
                                            }

                                            if (context != null)
                                            {
                                                context.runOnUiThread(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        if(pb!=null)
                                                            pb.setVisibility(View.GONE);

                                                        txtCode.setText(code);
                                                        txtCode.setTypeface(null, Typeface.BOLD_ITALIC);
                                                        expireLbl.setTypeface(null, Typeface.BOLD);
                                                        redimLbl.setTypeface(null, Typeface.BOLD);
                                                        if (bitmap != null)
                                                            qrCode.setImageBitmap(bitmap);
                                                    }
                                                });
                                            }
                                        } else
                                        {
                                            Log.d(TAG, response);
                                            Toast.makeText(context, response, Toast.LENGTH_LONG);
                                        }
                                    }else
                                    {
                                        Toast.makeText(context, "You are not signed into any event.", Toast.LENGTH_LONG);
                                        Log.d(TAG, "You are not signed into any event.");
                                    }
                                }else
                                {
                                    Toast.makeText(context,"You are not signed into any event.",Toast.LENGTH_LONG);
                                    Log.d(TAG, "You are not signed into any event.");
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
            });
        }

        public View getView()
        {
            return this.view;
        }

        public TextView getRewardName()
        {
            return this.mRewardName;
        }

        public TextView getRewardDescription()
        {
            return this.mRewardDescription;
        }

        public ImageView getRewardIcon()
        {
            return this.mRewardIcon;
        }

        public TextView getValue()
        {
            return this.mValue;
        }

        public Button getClaimButton()
        {
            return this.btnClaim;
        }

        public Reward getReward()
        {
            return this.reward;
        }

        public void setReward(Reward reward)
        {
            if(reward!=null)
            {
                this.reward = reward;
                setRewardName(reward.getRwName());
                setRewardDescription(reward.getRwDescription());
                setRewardValue(String.valueOf(reward.getRwCost()));
            }else Log.wtf(TAG, "Reward is null.");
        }

        public void setRewardName(String rwName) {this.mRewardName.setText(rwName);}

        public void setRewardDescription(String description) {this.mRewardDescription.setText(description);}

        public void setRewardIcon(Bitmap bmp) {this.mRewardIcon.setImageBitmap(bmp);}

        public void setRewardValue(String value) {this.mValue.setText(value);}

        @Override
        public String toString()
        {
            return super.toString() + " '" + mRewardName.getText() + "'";
        }
    }
}
