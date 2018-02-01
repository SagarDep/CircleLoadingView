package com.tgithubc.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;


/**
 * Created by tc :)
 */
public class CircleLoadingView extends View {

    private Paint mPaint;
    private float mWidth, mHeight;
    private int mSize;
    private float mRadius;// 圆角矩形的圆角半径，通过更改此值，由圆平滑过度到正方形
    private float mSmallRoundSize;// 小圆角矩形的宽高，通过更改此值，由小圆平滑过度到大圆
    private RectF mRectF;
    private AnimatorSet mAnimator;

    public CircleLoadingView(Context context) {
        this(context, null);
    }

    public CircleLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 其实从头到尾只需要绘制圆角矩形就行
        canvas.drawRoundRect(mRectF, mRadius, mRadius, mPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 测量之后的宽高
        mWidth = w;
        mHeight = h;
        // 初始化小圆角矩形的矩形参数
        // 小圆角矩形的宽高和大圆角矩形内切圆的直径也就是大圆角矩形的边长的关系应该是 2 * x平方 = w的平方
        // 小圆角矩形的宽高sWidth为Math.sqrt(Math.pow(mWidth, 2) / 2) 左上角的起始点应该为 w/2 - sWidth /2
        mSmallRoundSize = (float) Math.sqrt(Math.pow(mWidth, 2) / 2);
        resetSize();
    }

    private void resetSize() {
        mRadius = mSmallRoundSize / 2;
        float left = (mWidth - mSmallRoundSize) / 2;
        float top = (mHeight - mSmallRoundSize) / 2;
        mRectF = new RectF(left, top, left + mSmallRoundSize, top + mSmallRoundSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = resolveSize(mSize, widthMeasureSpec);
        int measuredHeight = resolveSize(mSize, heightMeasureSpec);
        int size = Math.min(measuredWidth, measuredHeight);
        setMeasuredDimension(size, size);
    }

    public void startAnimator() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mAnimator = new AnimatorSet();
        mAnimator.play(getScaleAnimator()).with(getSquareAnimator()).before(getRotateAnimator());
        mAnimator.addListener(new AnimatorListenerAdapter() {

            boolean cancelled = false;

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!cancelled) {
                    startAnimator();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }
        });
        mAnimator.start();
    }

    public void stopAnimation() {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        // 恢复到初始状态
        resetSize();
        invalidate();
    }

    @NonNull
    private ObjectAnimator getRotateAnimator() {
        // 旋转，过程中再变到圆形
        ObjectAnimator rotate = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f);
        rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                // 当前转过的角度占比，等同于需要变化的圆角矩形的变化率
                mRadius = (mWidth / 2) * (value / 360f);
                invalidate();
            }
        });
        rotate.setInterpolator(new AccelerateInterpolator());
        rotate.setDuration(600);
        return rotate;
    }

    @NonNull
    private ValueAnimator getSquareAnimator() {
        // 圆角角度由边长的一半过渡到0，就是由圆变正方形到过程
        ValueAnimator square = ValueAnimator.ofFloat(mWidth / 2, 0);
        square.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // 圆角矩形的圆角弧度
                mRadius = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        square.setDuration(800);
        return square;
    }

    @NonNull
    private ValueAnimator getScaleAnimator() {
        // 从小圆角矩形变到大圆角矩形。小矩形边长过渡到大矩形边长
        ValueAnimator scale = ValueAnimator.ofFloat(mSmallRoundSize / 2, mWidth / 2);
        scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // 实际上就是变化中的新边长
                float value = (float) valueAnimator.getAnimatedValue();
                // 左上角的位置
                float newPoint = mWidth / 2 - value;
                mRectF.set(newPoint, newPoint, value * 2 + newPoint, value * 2 + newPoint);
                mRadius = value;
                invalidate();
            }
        });
        scale.setDuration(800);
        return scale;
    }

    public void setSize(int size) {
        this.mSize = size;
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }
}