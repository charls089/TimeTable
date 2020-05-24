package com.kobbi.view.timetable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class DisableTimeView extends View {
    private Paint mDiagonalLine = new Paint();
    private Path mPath = new Path();

    public DisableTimeView(Context context) {
        super(context);
    }

    public DisableTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mDiagonalLine.reset();
        mPath.reset();
        canvas.drawColor(Color.argb(90, 50, 50, 50));
        mDiagonalLine.setStrokeWidth(8);
        mDiagonalLine.setStyle(Paint.Style.STROKE);
        mDiagonalLine.setColor(Color.argb(90, 255, 255, 255));
        int width = getWidth();
        int height = getHeight();
        float term = 30;

        for (int i = 0; i * term <= height; i++) {
            for (int j = 0; j * term <= width; j++) {
                mPath.moveTo(j * term, i * term);
                mPath.lineTo((j + 1) * term, (i + 1) * term);
            }
        }
        canvas.drawPath(mPath, mDiagonalLine);
    }
}