package com.opensource.focusview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Use:
 * Created by yinglovezhuzhu@gmail.com on 2014-08-01.
 */
public class FocusView extends View {

    private static final int STATE_IDLE     = 0;
    private static final int STATE_FOCUSING = 1;
    private static final int STATE_SUCCESS  = 2;
    private static final int STATE_FAILED   = 3;

    private static final int MSG_UPDATE_DRAW = 0;

    private Paint mPaint;
    private LocalHandler mHandler = new LocalHandler();
    private int mNormalColor = Color.argb(0xFF, 0xFF, 0xFF, 0xFF);
    private int mSuccessColor = Color.argb(0xFF, 0x00, 0xFF, 0x00);
    private int mFailedColor = Color.argb(0xFF, 0xFF, 0x00, 0x00);
    private float mStrokeWidth = 3.0f;
    private int mMinRadius = 50;
    private int mMaxRadius = 80;
    private int mCurrentRadius = 80 - (int) mStrokeWidth;
    private PointF mCenter = new PointF(mMaxRadius, mMaxRadius);

    private int mState = STATE_IDLE;

    private boolean mEnabled = true;

    private OnLongTouchListener mLongTouchListener;


    public FocusView(Context context) {
        super(context);
        init();
    }

    public FocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (mState) {
            case STATE_IDLE:    //空闲状态
                return;
            case STATE_FOCUSING://正在对焦
                mPaint.setColor(mNormalColor);
                break;
            case STATE_SUCCESS://对焦成功
                mPaint.setColor(mSuccessColor);
                break;
            case STATE_FAILED://对焦失败
                mPaint.setColor(mFailedColor);
                break;
            default:
                mPaint.setColor(mNormalColor);
                break;
        }
        canvas.drawCircle(mCenter.x, mCenter.y, mCurrentRadius, mPaint);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mEnabled) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mCenter.x = event.getX();
                    mCenter.y = event.getY();
                    fixCenterPoint();
                    mCurrentRadius = mMaxRadius;
                    mState = STATE_FOCUSING;
                    invalidate();
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DRAW, 20);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public void focusSuccessed() {
        mState = STATE_SUCCESS;
    }

    public void focusFailed() {
        mState = STATE_FAILED;
    }

    public void setOnLongTouchListener(OnLongTouchListener l) {
        this.mLongTouchListener = l;
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);

    }

    /**
     * 更正圆心点，保证能够绘制整个园
     */
    private void fixCenterPoint() {
        mCenter.x = mCenter.x < mMaxRadius + mStrokeWidth ? mMaxRadius + mStrokeWidth : mCenter.x;
        mCenter.x = mCenter.x > getWidth() - mMaxRadius - mStrokeWidth ? getWidth() - mMaxRadius - mStrokeWidth: mCenter.x;
        mCenter.y = mCenter.y < mMaxRadius + mStrokeWidth ? mMaxRadius + mStrokeWidth : mCenter.y;
        mCenter.y = mCenter.y > getHeight() - mMaxRadius - mStrokeWidth ? getHeight() - mMaxRadius - mStrokeWidth : mCenter.y;
    }

    private class LocalHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_DRAW:
                    if(mCurrentRadius > mMinRadius) {
                        mCurrentRadius -= 2;
                        if(mCurrentRadius < mMinRadius + 5) {
                            focusSuccessed();
                        }
                        invalidate();
                        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DRAW, 20);
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public static interface OnLongTouchListener {
        public void onLongTouch(View view);
    }
}