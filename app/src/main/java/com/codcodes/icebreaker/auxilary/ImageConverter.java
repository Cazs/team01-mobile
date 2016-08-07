package com.codcodes.icebreaker.auxilary;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.codcodes.icebreaker.tabs.EventsFragment;

/**
 * Created by tevin on 2016/07/13.
 */
public class ImageConverter
{
    public static Bitmap getRoundedCornerBitMap(Bitmap bitmap,int pixels)
    {
        if(bitmap!=null) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPX = pixels;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        }
        else
        {
            Log.d(EventsFragment.TAG,"Can't get rounded image because the Bitmap object is null");
            return  null;
        }
    }
}