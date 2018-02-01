package com.tgithubc.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by tc :)
 */
public class CircleLoadingLayout extends ViewGroup {

    private CircleLoadingView mLoading;
    private TextView mLoadingText;
    private int mDefaultMargin; // 预留给里面的球一个margin,不然转动起来变成正方形会超出范围，就不需要球自己去设margin了

    public CircleLoadingLayout(Context context) {
        this(context, null);
    }

    public CircleLoadingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleLoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        int defaultDiameter = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32,
                getResources().getDisplayMetrics());
        int defaultTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5,
                getResources().getDisplayMetrics());
        mDefaultMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                getResources().getDisplayMetrics());
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleLoadingViewStyle, 0, 0);
        int diameter, circleColor;
        int textColor, textSize;
        try {
            diameter = ta.getDimensionPixelOffset(R.styleable.CircleLoadingViewStyle_loading_circle_diameter,
                    defaultDiameter);
            circleColor = ta.getColor(R.styleable.CircleLoadingViewStyle_loading_circle_color, Color.BLACK);
            textSize = ta.getDimensionPixelOffset(R.styleable.CircleLoadingViewStyle_loading_text_size,
                    defaultTextSize);
            textColor = ta.getColor(R.styleable.CircleLoadingViewStyle_loading_text_color, Color.WHITE);
        } finally {
            ta.recycle();
        }

        mLoading = new CircleLoadingView(context);
        mLoading.setSize(diameter);
        mLoading.setColor(circleColor);
        addView(mLoading);

        mLoadingText = new TextView(context);
        mLoadingText.setTextColor(textColor);
        mLoadingText.setTextSize(textSize);
        mLoadingText.getPaint().setFakeBoldText(true);
        addView(mLoadingText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;
        int count;
        if ((count = getChildCount()) > 0) {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                // 应该以圆的宽高为宽高基准，字居其中，超过圆的范围大小的字是不合理的布局使用，不用考虑
                if (i == 0) {
                    width = child.getMeasuredWidth() + mDefaultMargin;
                    height = child.getMeasuredHeight() + mDefaultMargin;
                }
            }
            width += getPaddingLeft() + getPaddingRight();
            height += getPaddingTop() + getPaddingBottom();

            width = Math.max(width, getSuggestedMinimumWidth());
            height = Math.max(height, getSuggestedMinimumHeight());
        }
        setMeasuredDimension(width, height);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int parentLeft = getPaddingLeft();
        final int parentRight = r - l - getPaddingRight();
        final int parentTop = getPaddingTop();
        final int parentBottom = b - t - getPaddingBottom();
        if (getChildCount() > 1) {
            View circle = getChildAt(0);
            // 圆以父布局除去padding之后的位置开始布局
            circle.layout(parentLeft + mDefaultMargin,
                    parentTop + mDefaultMargin,
                    parentLeft + circle.getMeasuredWidth(),
                    parentTop + circle.getMeasuredHeight());
            View text = getChildAt(1);
            // 文字以父布局除去padding之后的位置,然后在父布局居中开始布局
            int textLeft = parentLeft + (parentRight - parentLeft - text.getMeasuredWidth()) / 2;
            int textTop = parentTop + (parentBottom - parentTop - text.getMeasuredHeight()) / 2;
            text.layout(textLeft, textTop, textLeft + text.getMeasuredWidth(), textTop + text.getMeasuredHeight());
        }
    }


    public void startAnimator() {
        mLoading.startAnimator();
    }

    public void stopAnimation() {
        mLoading.stopAnimation();
        setProgress(-1);
    }

    public void setProgress(int progress) {
        if (progress == -1) {
            mLoadingText.setText("");
        } else {
            mLoadingText.setText(String.valueOf(progress));
        }
    }
}