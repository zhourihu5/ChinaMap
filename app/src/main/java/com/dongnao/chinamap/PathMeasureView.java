package com.dongnao.chinamap;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class PathMeasureView  extends View {
    private Path path,dst;
    private Paint mPaint;
    private float length;
    private PathMeasure pathMeasure;
    private float mAnimatorValue;
    private float[] pos = new float[2];
    public PathMeasureView(Context context) {
        super(context);
    }

    public PathMeasureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        pathMeasure = new PathMeasure();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        path = new Path();
        dst = new Path();
        path.addCircle(400, 400, 100, Path.Direction.CW);
        pathMeasure.setPath(path, true);
        length = pathMeasure.getLength();
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAnimatorValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setDuration(2000);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.start();

    }

    public PathMeasureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


    }
//不断重绘
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        dst.reset();
        dst.lineTo(0, 0);
//            stop  不断计算长度
        float stop = length * mAnimatorValue;
        pathMeasure.getSegment(0, stop, dst, true);
        pathMeasure.getPosTan(stop, pos, null);
        canvas.drawPath(dst, mPaint);
//        加载小圆圈
        canvas.drawCircle(pos[0],pos[1],25,mPaint);
    }
}
