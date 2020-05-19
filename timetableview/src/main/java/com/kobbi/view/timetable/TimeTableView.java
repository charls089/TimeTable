package com.kobbi.view.timetable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TimeTableView extends LinearLayout {
    private static final int HEADER_HEIGHT_DP = 64;
    private static final int HEADER_TEXT_SIZE = 28;
    private static final int BOX_HEIGHT_DP = 24;
    private static final int TIME_WIDTH_DP = 80;
    private static final int TIME_MARGIN_DP = 5;
    private static final int TIME_TEXT_SIZE = 16;
    private static final int ROW_COUNT = 24;
    private static final int TIME_TERM = 2;

    private static final int TYPE_DAY = 0;
    private static final int TYPE_WEEK = 1;
    private static final String[] WEEK_NAMES = {"월", "화", "수", "목", "금", "토", "일"};

    private int rowCount;
    private int columnCount;
    private int headerHeight;
    private int boxHeight;
    private int timeWidth;
    private int timeTerm;
    private int boxColor;
    private int boxLineColor;

    private RelativeLayout mTimeTableBoxContainer;
    private LinearLayout mCurrentTimeView;
    private TimeTableEventListener mEventListener;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.0f;

    private final ArrayList<TableRow> mBoxRowList = new ArrayList<>();
    private final ArrayList<TextView> mSideTimeViewList = new ArrayList<>();
    private final HashMap<Schedule, View> mScheduledViewMap = new HashMap<>();

    public TimeTableView(Context context) {
        super(context);
    }

    public TimeTableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setAttributes(attrs);
        init();
    }

    public void setEventListener(TimeTableEventListener listener) {
        mEventListener = listener;
    }

    public void addSchedule(Schedule schedule) {
        Context context = getContext();
        View view = new View(context);
        view.setLayoutParams(getSharedTimeLayoutParams(schedule, boxHeight));
        int border = schedule.isEnabled() ? R.drawable.border_share_enabled : R.drawable.border_share_stop;
        view.setBackground(ContextCompat.getDrawable(context, border));
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEventListener != null)
                    mEventListener.onClick();
            }
        });
        synchronized (mScheduledViewMap) {
            mScheduledViewMap.put(schedule, view);
        }
        mTimeTableBoxContainer.addView(view);
    }

    public void removeSchedule(Schedule schedule) {
        synchronized (mScheduledViewMap) {
            mScheduledViewMap.remove(schedule);
        }
    }

    private void setAttributes(AttributeSet attrs) {
        Context context = getContext();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeTableView);
        rowCount = typedArray.getInt(R.styleable.TimeTableView_row_count, ROW_COUNT);
        int type = typedArray.getInteger(R.styleable.TimeTableView_week_type, TYPE_WEEK);
        columnCount = (type == TYPE_WEEK) ? 7 : 1;
        headerHeight = typedArray.getInt(R.styleable.TimeTableView_header_height, convertDpToPx(HEADER_HEIGHT_DP));
        boxHeight = typedArray.getInt(R.styleable.TimeTableView_box_height, convertDpToPx(BOX_HEIGHT_DP));
        timeWidth = typedArray.getInt(R.styleable.TimeTableView_time_width, convertDpToPx(TIME_WIDTH_DP));
        timeTerm = typedArray.getInt(R.styleable.TimeTableView_time_term, TIME_TERM);
        boxColor = typedArray.getColor(R.styleable.TimeTableView_box_color, Color.WHITE);
        boxLineColor = typedArray.getColor(R.styleable.TimeTableView_box_line_color, Color.GRAY);
        typedArray.recycle();
    }

    private void init() {
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mScaleFactor *= mScaleDetector.getScaleFactor();
                mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 2.0f));
                int changeBoxHeight = (int) (boxHeight * mScaleFactor);
                Log.e("####", "mScaleFactor : " + mScaleFactor + ", changeBoxHeight : " + changeBoxHeight);
                zoomInOut(changeBoxHeight);
                return true;
            }
        });
        setBackgroundColor(boxColor);
        createTableHeader();
        createTimeTableContainer();
    }

    private void zoomInOut(int boxHeight) {
        //Resizing table box
        for (TableRow row : mBoxRowList) {
            for (int i = 0; i < row.getChildCount(); i++) {
                row.getChildAt(i).setLayoutParams(getTableRowLayoutParams(boxHeight));
            }
        }
        //Resizing scheduled view
        for (Map.Entry<Schedule, View> entry : mScheduledViewMap.entrySet()) {
            Schedule schedule = entry.getKey();
            View view = entry.getValue();
            view.setLayoutParams(getSharedTimeLayoutParams(schedule, boxHeight));
        }
        //Resizing side time view
        for (int i = 0; i < mSideTimeViewList.size(); i++) {
            mSideTimeViewList.get(i).setLayoutParams(getSideTimeViewParams(i, timeTerm, boxHeight));
        }
        //change current time view position
        mCurrentTimeView.setLayoutParams(getCurrentTimeViewParams(boxHeight));
        invalidate();
    }

    private void createTableHeader() {
        Context context = getContext();
        TableLayout tableLayout = new TableLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(timeWidth, 0, 0, 0);
        tableLayout.setLayoutParams(params);
        TableRow row = new TableRow(context);
        row.setLayoutParams(getTableLayoutParams());
        for (int col = 0; col < columnCount; col++) {
            TextView child = new TextView(context);
            child.setLayoutParams(getTableRowLayoutParams(headerHeight));
            child.setText(WEEK_NAMES[col]);
            child.setGravity(Gravity.CENTER);
            child.setBackgroundColor(boxColor);
            child.setTextSize(TypedValue.COMPLEX_UNIT_SP, HEADER_TEXT_SIZE);
            row.addView(child);
        }
        tableLayout.addView(row);
        addView(tableLayout);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createTimeTableContainer() {
        Context context = getContext();
        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mTimeTableBoxContainer = new RelativeLayout(context);
        mTimeTableBoxContainer.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mTimeTableBoxContainer.setBackgroundColor(boxColor);
        mTimeTableBoxContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("####", "onTouch() --> getPointerCount :  " + event.getPointerCount());
                if (event.getPointerCount() == 2) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        break;
//                }
                mScaleDetector.onTouchEvent(event);
                return true;
            }
        });

        createTableBox();
        createBottomLineView();
        createSideTimeView();
        createCurrentTimeBar();

        scrollView.addView(mTimeTableBoxContainer);
        addView(scrollView);
    }

    private void createTableBox() {
        Context context = getContext();
        TableLayout tableLayout = new TableLayout(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(timeWidth, 0, 0, 0);
        tableLayout.setLayoutParams(params);
        tableLayout.setBackgroundColor(boxLineColor);
        for (int tr = 0; tr < rowCount + 2; tr++) {
            TableRow row = new TableRow(context);
            row.setLayoutParams(getTableLayoutParams());
            for (int col = 0; col < columnCount; col++) {
                View child = new View(context);
                child.setLayoutParams(getTableRowLayoutParams(boxHeight));
                child.setBackgroundColor(boxColor);
                row.addView(child);
            }
            mBoxRowList.add(row);
            tableLayout.addView(row);
        }
        mTimeTableBoxContainer.addView(tableLayout);
    }

    private void createBottomLineView() {
        Context context = getContext();
        View bottomLine = new View(context);
        bottomLine.setLayoutParams(getBottomLineViewParams());
        bottomLine.setBackgroundColor(boxColor);
        mTimeTableBoxContainer.addView(bottomLine);
    }

    private void createSideTimeView() {
        Context context = getContext();
        for (int i = 0; i <= rowCount / timeTerm; i++) {
            TextView textView = new TextView(context);
            textView.setLayoutParams(getSideTimeViewParams(i, timeTerm, boxHeight));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TIME_TEXT_SIZE);
            textView.setPadding(convertDpToPx(TIME_MARGIN_DP), 0, convertDpToPx(TIME_MARGIN_DP), 0);
            textView.setGravity(Gravity.CENTER);
            textView.setBackgroundColor(boxColor);
            int time = i * timeTerm;
            String amPm;
            if (time < 12) {
                amPm = "오전";
            } else if (time == 12) {
                amPm = "정오";
            } else if (time < 23) {
                time = time % 12;
                amPm = "오후";
            } else {
                time = 12;
                amPm = "자정";
            }
            textView.setText(String.format(Locale.getDefault(), "%s %d시", amPm, time));
            mSideTimeViewList.add(textView);
            mTimeTableBoxContainer.addView(textView);
        }
    }

    private void createCurrentTimeBar() {
        Context context = getContext();
        mCurrentTimeView = new LinearLayout(context);
        mCurrentTimeView.setOrientation(HORIZONTAL);
        mCurrentTimeView.setLayoutParams(getCurrentTimeViewParams(boxHeight));
        mCurrentTimeView.setGravity(Gravity.CENTER);
        View line = new View(context);
        line.setLayoutParams(new LinearLayout.LayoutParams(getBoxWidth(), 4));
        line.setBackgroundResource(R.drawable.ic_current_point);
        View point = new View(context);
        point.setLayoutParams(new LinearLayout.LayoutParams(12, 12));
        point.setBackgroundResource(R.drawable.ic_current_point);
        point.setRotation(45);
        mCurrentTimeView.addView(line);
        mCurrentTimeView.addView(point);
        mTimeTableBoxContainer.addView(mCurrentTimeView);
    }

    private RelativeLayout.LayoutParams getCurrentTimeViewParams(int boxHeight) {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.setMargins(timeWidth + getDayOfWeekPosition(dayOfWeek) * (getBoxWidth() + 1), calTimePosition(Schedule.getTime(hour, min), boxHeight), 0, 0);
        return params;
    }

    private RelativeLayout.LayoutParams getSideTimeViewParams(int position, int term, int boxHeight) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(timeWidth, (1 + boxHeight) * 2);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.setMargins(0, term * position * (boxHeight + 1), 0, 0);
        return params;
    }

    private RelativeLayout.LayoutParams getBottomLineViewParams() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        return params;
    }

    private TableLayout.LayoutParams getTableLayoutParams() {
        return new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
    }

    private TableRow.LayoutParams getTableRowLayoutParams(int height) {
        TableRow.LayoutParams params = new TableRow.LayoutParams(getBoxWidth(), height);
        params.setMargins(0, 0, 1, 1);
        return params;
    }

    private RelativeLayout.LayoutParams getSharedTimeLayoutParams(Schedule schedule, int boxHeight) {
        int width = getBoxWidth() - 4;
        int height = getSharedTimeBoxHeight(schedule.getStartTime(), schedule.getEndTime(), boxHeight) - 4;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int leftMargin = 2 + timeWidth + getDayOfWeekPosition(schedule.getDayOfWeek()) * (getBoxWidth() + 1);
        int topMargin = 2 + calTimePosition(schedule.getStartTime(), boxHeight);
        params.setMargins(leftMargin, topMargin, 0, 0);
        return params;
    }

    private int getSharedTimeBoxHeight(Schedule.Time startTime, Schedule.Time endTime, int boxHeight) {
        return calTimePosition(endTime, boxHeight) - calTimePosition(startTime, boxHeight);
    }

    private int calTimePosition(Schedule.Time time, int boxHeight) {
        int hour = time.getHour();
        int min = time.getMin();
        return hour + boxHeight * (hour + 1) + (boxHeight / 60) * min;
    }

    private int getDayOfWeekPosition(int dayOfWeek) {
        if (dayOfWeek == Calendar.SUNDAY)
            return 6;
        else
            return dayOfWeek - 2;
    }

    private int convertDpToPx(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private int getBoxWidth() {
        Context context = getContext();
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return (size.x - timeWidth) / columnCount;
    }

    interface TimeTableEventListener {
        void onClick();
    }
}
