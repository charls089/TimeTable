package com.kobbi.view.timetable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class DisableTimeView extends View {
    Paint diagonalLine = new Paint();
    Path path = new Path();

    public DisableTimeView(Context context) {
        super(context);
    }

    public DisableTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.argb(90, 50, 50, 50));
        diagonalLine.setStrokeWidth(8);
        diagonalLine.setStyle(Paint.Style.STROKE);
        diagonalLine.setColor(Color.argb(90, 255, 255, 255));
        int width = getWidth();
        int height = getHeight();
        float term = 30;

        for (int i = 0; i * term <= height; i++) {
            for (int j = 0; j * term <= width; j++) {
                path.moveTo(j * term, i * term);
                path.lineTo((j + 1) * term, (i + 1) * term);
            }
        }
        canvas.drawPath(path, diagonalLine);
    }
}