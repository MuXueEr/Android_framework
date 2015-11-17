package com.android.libcore_ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.libcore.log.L;
import com.android.libcore_ui.R;

/**
 * Description: 流式布局
 *
 * @author zzp(zhao_zepeng@hotmail.com)
 * @since 2015-11-14
 */
public class FlowLayout extends ViewGroup{
    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;
    //默认间隙
    private int verticalSpacing = 10;
    private int horizontalSpacing = 10;
    //布局方向
    private int orientation = HORIZONTAL;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrValue(attrs);
    }

    private void getAttrValue(AttributeSet attrs){
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FlowLayout);

        verticalSpacing = typedArray.getDimensionPixelSize(R.styleable.FlowLayout_verticalSpacing, 10);
        horizontalSpacing = typedArray.getDimensionPixelSize(R.styleable.FlowLayout_horizontalSpacing, 10);
        orientation = typedArray.getInt(R.styleable.FlowLayout_orientation, HORIZONTAL);

        typedArray.recycle();
        //TODO  2.修改子view布局gravity 3.编写博客，介绍关于flowlayout和attr的相关知识
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() <= 0){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int width;
        int height;
        //水平布局，宽度固定，高度变化
        if (orientation == HORIZONTAL) {
            width = MeasureSpec.getSize(widthMeasureSpec);
            height = 0;
        }
        //垂直布局，高度固定，宽度变化
        else{
            width = 0;
            height = MeasureSpec.getSize(heightMeasureSpec);
        }

        int childWidth;
        int childHeight;

        //该行最大子view大小
        int maxChildSize = 0;
        //剩余大小
        int lastSize;
        if (orientation == HORIZONTAL) {
            lastSize = width - paddingLeft - paddingRight;
        }else{
            lastSize = height - paddingTop - paddingBottom;
        }

        int x = paddingLeft;
        int y = paddingTop;

        int childSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            childHeight = lp.height;
            childWidth = lp.width;

            if (childHeight <= 0 || childWidth <= 0) {
                child.measure(childSpec, childSpec);
                childWidth = child.getMeasuredWidth();
                childHeight = child.getMeasuredHeight();
            }
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));

            if (orientation == HORIZONTAL) {
                lastSize = lastSize - childWidth - horizontalSpacing;
            }else{
                lastSize = lastSize - childHeight - verticalSpacing;
            }

            //如果第一个子view的大小已经超过容器大小
            if (i==0 && lastSize <0) {
                throw new ChildSizeTooLongException("the " + i + " child's width/height too long");
            }

            //需要换行
            if (lastSize < 0) {
                //将大小重置
                if (orientation == HORIZONTAL) {
                    lastSize = width - paddingLeft - paddingRight - childWidth;
                }else{
                    lastSize = height - paddingTop - paddingBottom - childHeight;
                }
                //换行之后该行的第一个view宽度超过整体父view宽度
                if (lastSize < 0){
                    throw new ChildSizeTooLongException("the " + i + " child's width/height too long");
                }

                if (orientation == HORIZONTAL) {
                    //高换行
                    height += maxChildSize + verticalSpacing;
                    //换行之后的第一行坐标
                    x = paddingLeft;
                    y += maxChildSize + verticalSpacing;
                    //将最大高度值置为这第一个view的高度
                    maxChildSize = childHeight;
                }else{
                    //宽换列
                    width += maxChildSize + horizontalSpacing;
                    //换列之后的第一列坐标
                    x += maxChildSize + horizontalSpacing;
                    y = paddingTop;
                    //将最大宽度值置为这第一个view的宽度
                    maxChildSize = childWidth;
                }
            }
            //不需要换行
            else {
                if (orientation == HORIZONTAL) {
                    //计算出这一行子view中高度最大的view
                    maxChildSize = maxChildSize > childHeight ? maxChildSize : childHeight;
                }else{
                    //计算出这一列子view中宽度最大的view
                    maxChildSize = maxChildSize > childWidth ? maxChildSize : childWidth;
                }
            }
            lp.setXY(x, y);
            if (orientation == HORIZONTAL) {
                x += childWidth + horizontalSpacing;
            }else{
                y += childHeight + verticalSpacing;
            }
        }
        if (orientation == HORIZONTAL) {
            height += maxChildSize;
            height += + paddingBottom + paddingTop;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }else{
            width += maxChildSize;
            width += paddingLeft + paddingRight;
        }
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.layout(lp.x, lp.y, lp.x + child.getMeasuredWidth(), lp.y + child.getMeasuredHeight());
        }
    }

    /**
     * 设置布局方向
     * @param orientation {@link #HORIZONTAL}or{@link #VERTICAL}
     */
    public void setOrientation(int orientation){
        if (orientation!=HORIZONTAL && orientation!=VERTICAL)
            throw new IllegalArgumentException("orientation error");
        this.orientation = orientation;
        invalidate();
    }

    public int getOrientation(){
        return orientation;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams{
        public int x;
        public int y;

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public void setXY(int x, int y){
            this.x = x;
            this.y = y;
        }
    }

    public static class ChildSizeTooLongException extends RuntimeException{
        public ChildSizeTooLongException(String message){
            super(message);
        }
    }
}
