package com.codcodes.icebreaker.auxilary;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.MainActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casper on 2016/11/01.
 */
/**
 * {@link RecyclerView.Adapter} that can display a {@link User} and makes a call to the
 * specified {@link IOnListFragmentInteractionListener}.
 */
public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>
{
    //private final List<Bitmap> mThumbnails;
    private final String TAG = "IB/TimelineAdapter";
    private Activity context;
    private boolean isScreenTouched = false;
    private List<AbstractMap.SimpleEntry<String, String>> image_to_audio_map;

    private final IOnListFragmentInteractionListener mListener;

    public TimelineAdapter(Activity context, List<AbstractMap.SimpleEntry<String, String>> aud_map, IOnListFragmentInteractionListener listener)
    {
        mListener = listener;
        //this.mThumbnails = mThumbnails;
        this.context=context;
        this.image_to_audio_map = aud_map;
    }

    @Override
    public TimelineViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timeline_item, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TimelineViewHolder holder, final int position)
    {
        //holder.mContactBio.setText(mValues.get(position).getCatchphrase());
        if(image_to_audio_map!=null)
        {
            if (position < image_to_audio_map.size())
            {
                final String path = "/public_res|events";
                final String filename = image_to_audio_map.get(position).getKey().split("\\.")[0];
                Thread tImageLoader = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final Bitmap bitmap;
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                        try
                        {
                            bitmap = LocalComms.getImage(context, filename, ".png", path, options);
                            if(context!=null)
                            {
                                context.runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        holder.setThumbnail(bitmap);
                                    }
                                });
                            }else Log.wtf(TAG,"Context is null.");
                        } catch (IOException e)
                        {
                            LocalComms.logException(e);
                        }
                    }
                });
                tImageLoader.start();
            } else Log.d(TAG, "Bitmap ArrayList is empty. Or index is out of bounds.");
        }

        holder.getView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mListener && context!=null)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    //mListener.onListFragmentInteraction(holder.getThumbnail());
                    Dialog dlg = new Dialog(context);
                    dlg.setContentView(R.layout.view_timeline_post);
                    dlg.setCancelable(true);
                    //dlg.getWindow().setLayout((int) (metrics.widthPixels * 0.90), metrics.widthPixels);

                    final DisplayMetrics metrics = new DisplayMetrics();
                    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    wm.getDefaultDisplay().getMetrics(metrics);
                    final LinearLayout img = (LinearLayout)dlg.findViewById(R.id.img_timeline_post);

                    if (image_to_audio_map != null)
                    {

                        //Bitmap bitmap = BitmapFactory.decodeFile(image_to_audio_map.get(position).getKey());
                        final String path = "/public_res|events";
                        final String filename = image_to_audio_map.get(position).getKey().split("\\.")[0];
                        Thread tImageLoader = new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final Bitmap bitmap;
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                try
                                {
                                    bitmap = LocalComms.getImage(context, filename, ".png", path, options);
                                    context.runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            img.setBackground(new BitmapDrawable(context.getResources(), bitmap));
                                            ViewGroup.LayoutParams img_params =  img.getLayoutParams();
                                            img_params.width = (int)(metrics.widthPixels * 1.20);
                                            img.setLayoutParams(img_params);
                                        }
                                    });
                                } catch (IOException e)
                                {
                                    LocalComms.logException(e);
                                }
                            }
                        });
                        tImageLoader.start();
                    }
                    dlg.show();

                    dlg.getWindow().setLayout((int)(metrics.widthPixels * 0.97),(int)(metrics.heightPixels*0.97));
                }else Log.wtf(TAG,"Context/Listener is null.");
            }
        });

        holder.getView().setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if(context!=null)
                {
                    Dialog dlg = new Dialog(context);
                    dlg.setContentView(R.layout.view_timeline_post);
                    dlg.setCancelable(true);
                    //dlg.getWindow().setLayout((int) (metrics.widthPixels * 0.90), metrics.widthPixels);

                    final DisplayMetrics metrics = new DisplayMetrics();
                    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    wm.getDefaultDisplay().getMetrics(metrics);
                    final LinearLayout img = (LinearLayout) dlg.findViewById(R.id.img_timeline_post);

                    Thread tAnimator = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (image_to_audio_map.size() > position)
                            {
                                for (int i = position; i < image_to_audio_map.size(); i++)
                                {
                                    final int index = i;
                                    try
                                    {
                                        if (image_to_audio_map != null)
                                        {
                                            context.runOnUiThread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    String path = "/public_res|events";
                                                    String filename = image_to_audio_map.get(index).getKey().split("\\.")[0];
                                                    Bitmap bitmap = null;
                                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                                    options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                                    try
                                                    {
                                                        bitmap = LocalComms.getImage(context, filename, ".png", path, options);
                                                    } catch (IOException e)
                                                    {
                                                        LocalComms.logException(e);
                                                    }

                                                    if (bitmap != null)
                                                    {
                                                        img.setBackground(new BitmapDrawable(context.getResources(), bitmap));

                                                        ViewGroup.LayoutParams img_params = img.getLayoutParams();
                                                        img_params.width = (int) (metrics.widthPixels * 1.20);
                                                        img.setLayoutParams(img_params);
                                                    } else Log.wtf(TAG, "Bitmap is null.");
                                                }
                                            });
                                            Thread.sleep(500);
                                        }
                                    } catch (InterruptedException e)
                                    {
                                        LocalComms.logException(e);
                                    }
                                }
                            }
                        }
                    });
                    tAnimator.start();

                    Thread tAudioPlayer = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            MediaPlayer player = new MediaPlayer();
                            for (int i = position; i < image_to_audio_map.size(); i++)
                            {
                                final int index = i;
                                try
                                {
                                    while (player.isPlaying()) {/*Log.v(TAG,"MediaPlayer is busy.");*/}//wait for playback to finish

                                    String path = "/public_res|events";
                                    String filename = image_to_audio_map.get(index).getValue().split("\\.")[0];
                                    String ext = image_to_audio_map.get(index).getValue().split("\\.")[1];

                                    byte[] audio = LocalComms.getFile(context, filename, "." + ext, path);
                                    if (audio != null)
                                    {
                                        Log.v(TAG, "Audio size for '" + filename + "." + ext + "': " + audio.length + " bytes.");
                                        audio = null;
                                        String data_src = MainActivity.rootDir + "/Icebreak" + path + "/" + filename + "." + ext;
                                        player.setDataSource(data_src);
                                        player.prepare();

                                        player.start();
                                    } else Log.d(TAG, "Audio file '" + filename + "." + ext + "' does not exist on server/locally.");
                                } catch (Exception e)
                                {
                                    LocalComms.logException(e);
                                }
                            }
                        }
                    });
                    tAudioPlayer.start();
                    dlg.show();

                    dlg.getWindow().setLayout((int) (metrics.widthPixels * 0.97), (int) (metrics.heightPixels * 0.97));
                }else Log.wtf(TAG,"Context is null.");
                return true;
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return image_to_audio_map.size();
    }

    public class TimelineViewHolder extends RecyclerView.ViewHolder
    {
        private final View view;
        private final LinearLayout mThumbnail;

        public TimelineViewHolder(View view)
        {
            super(view);
            this.view = view;
            this.mThumbnail = (LinearLayout) view.findViewById(R.id.thumbnail);
        }

        public void setThumbnail(Bitmap bitmap){if(this.mThumbnail!=null)this.mThumbnail.setBackground(new BitmapDrawable(context.getResources(),bitmap));}

        public LinearLayout getThumbnail()
        {
            return this.mThumbnail;
        }

        public View getView()
        {
            return this.view;
        }

        @Override
        public String toString()
        {
            return "";
        }
    }
}
