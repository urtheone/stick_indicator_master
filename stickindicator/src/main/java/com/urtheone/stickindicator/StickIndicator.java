package com.urtheone.stickindicator;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

public class StickIndicator extends View implements ViewPager.OnPageChangeListener {

    private static final float OFFSET_MULTIPLIER_DRAG = 1.2f;
    private static final float OFFSET_MULTIPLIER_SETTLING = 1.4f;
    private static final float OFFSET_MULTIPLIER_NORMAL = 0.30f;

    private static final int DEFAULT_HEIGHT = 4;
    private static final int DEFAULT_WIDTH = 20;
    private static final int DEFAULT_INTERVAL = 15;

    private static final int DEFAULT_ACTIVE_COLOR = 0xff5c5f;
    private static final int DEFAULT_INACTIVE_COLOR = 0xd5d5d5;

    private ViewPager mViewPager;
    private Paint activePaint;
    private Paint inactivePaint;

    private int mInterval;
    private int mHeight;
    private int mWidth;
    private int pointCount;

    private int mGravity;
    private int mState;
    private float mPageOffset;
    private int mCurrentDragPage;
    private int mSelectedPage;
    private float currentNormalOffset;
    private float currentRelativePageOffset;
    private float startedSettleNormalOffset;
    private float startedSettlePageOffset;
    private float radiusX;
    private float radiusY;

    public StickIndicator(Context context) {
        super(context);
        init(null);
    }

    public StickIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public StickIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StickIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        if (isInEditMode()) {
             pointCount = 3;
        }

        mHeight = dip2px(DEFAULT_HEIGHT);
        mWidth = dip2px(DEFAULT_WIDTH);
        mInterval = dip2px(DEFAULT_INTERVAL);
        radiusX = 0;
        radiusY = 0;

        int inactiveColor = ContextCompat.getColor(getContext(), R.color.indicator_inactive_color);
        int activeColor = ContextCompat.getColor(getContext(), R.color.indicator_active_color);
        int gravity = Gravity.CENTER;

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.StickIndicator, 0, 0);
            mHeight = (int) a.getDimension(R.styleable.StickIndicator_indicator_height, mHeight);
            mWidth = (int) a.getDimension(R.styleable.StickIndicator_indicator_width, mWidth);
            radiusX = (int) a.getDimension(R.styleable.StickIndicator_indicator_radiusX, radiusX);
            radiusY = (int) a.getDimension(R.styleable.StickIndicator_indicator_radiusY, radiusY);
            mInterval = (int) a.getDimension(R.styleable.StickIndicator_indicator_interval, mInterval);
            inactiveColor = a.getColor(R.styleable.StickIndicator_indicator_inactiveColor, inactiveColor);
            activeColor = a.getColor(R.styleable.StickIndicator_indicator_activeColor, activeColor);
            mGravity = a.getInt(R.styleable.StickIndicator_android_gravity, gravity);
        }

        activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activePaint.setColor(activeColor);

        inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        inactivePaint.setColor(inactiveColor);
    }

    public void setViewPager(ViewPager viewPager) {
        if(mViewPager == viewPager)
            return;
        if(mViewPager != null)
            viewPager.addOnPageChangeListener(this);
        if(viewPager.getAdapter() == null)
            throw new IllegalStateException("ViewPager doesn't have an adapter instance.");
        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(this);
        pointCount = viewPager.getAdapter().getCount();
        mCurrentDragPage = viewPager.getCurrentItem();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = View.resolveSize(getDesiredHeight(), heightMeasureSpec);
        int width = View.resolveSize(getDesiredWidth(), widthMeasureSpec);

        setMeasuredDimension(width, height);
    }

    private int getDesiredHeight() {
        return getPaddingTop() + getPaddingBottom() + mHeight;
    }

    private int getDesiredWidth() {
        return getPaddingLeft() + getPaddingRight() + mWidth * pointCount + ( pointCount - 1) * mInterval;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        for (int i = 0; i < pointCount; i++) {
            float center = getCircleCenter(i);
            RectF rectF = new RectF(-mWidth /2 + center, getPaddingTop(), mWidth /2 + center,getPaddingTop() + mHeight);
            canvas.drawRoundRect(rectF,radiusX,radiusY,inactivePaint);
        }

        drawRect(canvas);

    }

    private void drawRect(Canvas canvas) {

        if(mViewPager == null) return;

        if(mViewPager.getAdapter() == null) return;

        if(mViewPager.getAdapter().getCount() == 0) return;


        float top = getPaddingTop();
        float bottom = top + mHeight;

        float moveDistance = mWidth + mInterval;
        boolean isDragForward = mSelectedPage - mCurrentDragPage < 1;

        float relativePageOffset = isDragForward ? mPageOffset : 1.0f - mPageOffset;
        currentRelativePageOffset = relativePageOffset;

        float shiftedOffset = relativePageOffset * OFFSET_MULTIPLIER_NORMAL;

        float settleShiftedOffset = Math.max(0, mapValue(relativePageOffset, startedSettlePageOffset, 1.0f, startedSettleNormalOffset, 1.0f));

        float normalOffset = mState == ViewPager.SCROLL_STATE_SETTLING ? settleShiftedOffset : shiftedOffset;
        currentNormalOffset = normalOffset;

        float largerOffset = Math.min(mState == ViewPager.SCROLL_STATE_SETTLING ? relativePageOffset * OFFSET_MULTIPLIER_SETTLING : relativePageOffset * OFFSET_MULTIPLIER_DRAG, 1.0f);

        float circleCenter = getCircleCenter(isDragForward ? mCurrentDragPage : mSelectedPage);

        float normal = moveDistance * normalOffset;
        float large = moveDistance * largerOffset;

        float left = isDragForward ? circleCenter - mWidth /2 + normal : circleCenter - mWidth /2 - large;
        float right = isDragForward ? circleCenter + mWidth /2 + large : circleCenter + mWidth /2 - normal;

        RectF rectF = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rectF,radiusX,radiusY,activePaint);

    }

    private float mapValue(float value, float a1, float a2, float b1, float b2) {
        return b1 + (value - a1) * (b2 - b1) / (a2 - a1);
    }

    private float getCirclePadding(int position) {
        return mInterval * position + mWidth * position;
    }

    private float getCircleCenter(int position) {
        return getStartedX() + mWidth + getCirclePadding(position);
    }

    private float getStartedX() {
        switch (mGravity) {
            case Gravity.LEFT:
            case GravityCompat.START:
                return getPaddingLeft();
            case Gravity.RIGHT:
            case GravityCompat.END:
                return getMeasuredWidth() - getPaddingRight() - getAllCirclesWidth();
            case Gravity.CENTER:
            default:
                return (getMeasuredWidth() / 2 - getAllCirclesWidth() / 2);
        }
    }

    private float getAllCirclesWidth() {
        return mWidth * pointCount + ( pointCount - 1) * mInterval;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mCurrentDragPage = position;
        mPageOffset = positionOffset;
        postInvalidate();
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mState = state;
        if (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_DRAGGING) {
            mSelectedPage = mViewPager.getCurrentItem();
            currentNormalOffset = 0;
            currentRelativePageOffset = 0;
        } else if (state == ViewPager.SCROLL_STATE_SETTLING) {
            startedSettleNormalOffset = currentNormalOffset;
            startedSettlePageOffset = currentRelativePageOffset;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if(mViewPager != null)
            mViewPager.removeOnPageChangeListener(this);
        super.onDetachedFromWindow();
    }


    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
