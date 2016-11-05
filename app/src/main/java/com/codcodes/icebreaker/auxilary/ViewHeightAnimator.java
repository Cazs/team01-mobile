package com.codcodes.icebreaker.auxilary;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Casper on 2016/11/01.
 */
public class ViewHeightAnimator extends Animation
{
    final int targetHeight;
    View view;
    int startHeight;

    public ViewHeightAnimator(View view, int targetHeight, int startHeight)
    {
        this.view = view;
        this.targetHeight = targetHeight;
        this.startHeight = startHeight;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t)
    {
        //int newHeight = (int) (startHeight + targetHeight * interpolatedTime);
        int newHeight = (int) (startHeight+(targetHeight - startHeight) * interpolatedTime);
        view.getLayoutParams().height = newHeight;
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight)
    {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds()
    {
        return true;
    }
}
