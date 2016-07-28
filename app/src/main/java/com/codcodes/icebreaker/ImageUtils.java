package com.codcodes.icebreaker;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.ByteArrayOutputStream;

/**
 * Created by USER on 2016/07/24.
 */
public class ImageUtils
{
    public static ImageUtils mInstant;

    public static ImageUtils getInstant() {
        if (mInstant == null) {
            mInstant = new ImageUtils();
        }
        return mInstant;

    }
    public Bitmap compressBitmapImage(Resources res,int image)
    {
        float maxHeight = 816.0f;
        float maxWidth = 612.0f;

        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        Bitmap bmp = BitmapFactory.decodeResource(res,image,options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        float imgRatio = actualWidth/ actualHeight;
        float maxRatio = maxWidth /maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth)
        {
            if (imgRatio < maxRatio)
            {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio)
            {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }


        options.inSampleSize = calculateInSampleSize(options,actualWidth,actualHeight);

        options.inJustDecodeBounds = false;

        options.inPurgeable = true;
        options.inInputShareable =true;
        options.inTempStorage =new byte[16 * 1024];

        try
        {
            bmp = BitmapFactory.decodeResource(res,image,options);

        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        try
        {
            scaledBitmap = Bitmap.createBitmap(actualWidth,actualHeight,Bitmap.Config.ARGB_8888);

        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }

        float ratioX = actualWidth /(float) options.outWidth;
        float ratioY = actualHeight /(float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualWidth / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX,ratioY,middleX,middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp,middleX-bmp.getWidth() / 2 ,middleY-bmp.getHeight() / 2,new Paint(Paint.FILTER_BITMAP_FLAG));

        scaledBitmap = Bitmap.createBitmap(scaledBitmap,0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight(),scaleMatrix,true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG,100,out);

        byte[] bytearray = out.toByteArray();

        Bitmap compressedBitmap = BitmapFactory.decodeByteArray(bytearray,0,bytearray.length);
        return  compressedBitmap;
    }



    private int calculateInSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSamplesize = 1;

            if(height >reqHeight || width > reqWidth)
            {
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);
                inSamplesize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSamplesize * inSamplesize) > totalReqPixelsCap) {
                inSamplesize++;

            }
        return inSamplesize;
    }
}

