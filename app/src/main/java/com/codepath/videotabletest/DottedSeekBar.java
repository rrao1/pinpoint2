package com.codepath.videotabletest;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Created by kemleynieva on 7/7/16.
 */
public class DottedSeekBar extends SeekBar {

    /** Int values which corresponds to dots */
    private int[] mDotsPositions = null;
    /** Drawable for dot */
    private Bitmap mDotBitmap = null;
    /** Int value which corresponds to selected dot */
    private int mSelectedDot = -1;

    public DottedSeekBar(final Context context) {
        super(context);
        init(null);
    }

    public DottedSeekBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DottedSeekBar(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    /**
     * Initializes Seek bar extended attributes from xml
     *
     * @param attributeSet {@link AttributeSet}
     */
    private void init(final AttributeSet attributeSet) {
        final TypedArray attrsArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.DottedSeekBar, 0, 0);

        final int dotsArrayResource = attrsArray.getResourceId(R.styleable.DottedSeekBar_dots_positions, 0);

        if (0 != dotsArrayResource) {
            mDotsPositions = getResources().getIntArray(dotsArrayResource);
        }

        final int dotDrawableId = attrsArray.getResourceId(R.styleable.DottedSeekBar_dots_drawable, 0);

        if (0 != dotDrawableId) {
            mDotBitmap = BitmapFactory.decodeResource(getResources(), dotDrawableId);
        }
    }

    /**
     * @param dots to be displayed on this SeekBar
     */
    public void setDots(final int[] dots, int selectedDot) {
        mDotsPositions = dots;
        mSelectedDot = selectedDot;
        invalidate();
    }

    /**
     * @param dotsResource resource id to be used for dots drawing
     */
    public void setDotsDrawable(final int dotsResource) {
        mDotBitmap = BitmapFactory.decodeResource(getResources(), dotsResource);
        invalidate();
    }

    @Override
    protected synchronized void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final int width = getMeasuredWidth();
        final int paddingRight = getPaddingRight();
        final int paddingLeft = getPaddingLeft();
        final int barWidth = width - paddingLeft - paddingRight;
        final int step = (barWidth * 1000) / getMax();

        Paint paint = new Paint();
        int darkGray = Color.parseColor("#727272");
        paint.setColorFilter(new PorterDuffColorFilter(darkGray, PorterDuff.Mode.SRC_ATOP));

        if (null != mDotsPositions && 0 != mDotsPositions.length && null != mDotBitmap) {
            // draw dots if we have ones
            for (int position : mDotsPositions) {
                final int tagPosition = ((position * barWidth) / 1000);
                canvas.drawBitmap(mDotBitmap, tagPosition, 0, paint);
            }
        }

        if (mSelectedDot != -1) {
            int selectionColor = Color.parseColor("#FF4081");
            paint.setColorFilter(new PorterDuffColorFilter(selectionColor, PorterDuff.Mode.SRC_ATOP));
            final int tagPosition = ((mSelectedDot * barWidth) / 1000);
            canvas.drawBitmap(mDotBitmap, tagPosition, 0, paint);
        }
    }
}
