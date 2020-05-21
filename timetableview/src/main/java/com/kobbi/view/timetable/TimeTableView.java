package com.kobbi.view.timetable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TimeTableView extends LinearLayout {
    private static final int HEADER_HEIGHT_DP = 64;
    private static final int HEADER_TEXT_SIZE = 28;
    private static final int BOX_HEIGHT_DP = 24;
    private static final int SCHEDULE_MARGIN_DP = 1;
    private static final int TIME_WIDTH_DP = 80;
    private static final int TIME_MARGIN_DP = 5;
    private static final int TIME_TEXT_SIZE = 16;
    private static final int SCHEDULE_DEL_IMG_MARGIN = 16;
    private static final int DRAWING_SCHEDULE_TEXT_SIZE = 18;
    private static final int ROW_COUNT = 24;
    private static final int TIME_TERM_HOUR = 2;
    private static final int TYPE_DAY = 0;
    private static final int TYPE_WEEK = 1;
    private static final String[] WEEK_NAMES = {"월", "화", "수", "목", "금", "토", "일"};

    private static final int LONG_CLICK_TERM = 1000;

    private int rowCount;
    private int columnCount;
    private int headerHeight;
    private int boxHeight;
    private int timeWidth;
    private int timeTermHour;
    private int boxColor;
    private int boxLineColor;
    private int boxWidth;
    private int scheduleMargin;
    private int viewDayOfWeek;
    private int timeMargin;

    private RelativeLayout mTimeTableBoxContainer;
    private LinearLayout mCurrentTimeView;
    private TextView mDrawingView;
    private Schedule.Time mTouchTime;
    private Schedule mDrawingSchedule;
    private TimeTableEventListener mEventListener;
    private long mTouchTimeMills = 0;
    private boolean mIsLongClick = false;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.0f;
    private int mCurrentBoxHeight;

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

    /**
     * set TimeTableEventListener for receiving event
     *
     * @param listener TimeTableEventListener
     */
    public void setEventListener(TimeTableEventListener listener) {
        mEventListener = listener;
    }

    /**
     * add Schedule data and draw ScheduleView
     *
     * @param schedule schedule data
     */
    public void addSchedule(Schedule schedule) {
        Context context = getContext();
        RelativeLayout.LayoutParams params = getSharedTimeLayoutParams(schedule);
        View scheduleView;
        int borderResId = 0;
        switch (schedule.getType()) {
            case ACTIVATED:
            case EDIT:
                borderResId = (columnCount > 1) ? R.drawable.border_schedule_activated : R.drawable.border_schedule_edit_exist;
                break;
            case INACTIVATED:
                borderResId = (columnCount > 1) ? R.drawable.border_schedule_inactivated : R.drawable.border_schedule_edit_exist;
                break;
            case DISABLED:
                break;
            case UNSET:
                viewDayOfWeek = schedule.getDayOfWeek();
                return;
        }
        if (borderResId != 0) {
            RelativeLayout relativeLayout = new RelativeLayout(context);
            relativeLayout.setLayoutParams(params);
            relativeLayout.setBackground(ContextCompat.getDrawable(context, borderResId));

            if (columnCount == 1 && schedule.getType() != Schedule.Type.DISABLED) {
                TextView textView = new TextView(context);
                RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tvParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                textView.setLayoutParams(tvParams);
                textView.setText(convertTimeToString(schedule));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, DRAWING_SCHEDULE_TEXT_SIZE);
                textView.setTextColor(Color.WHITE);
                textView.setGravity(Gravity.CENTER);
                relativeLayout.addView(textView);
                ImageView imageView = new ImageView(context);
                RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                ivParams.setMargins(0, 0, convertDpToPx(SCHEDULE_DEL_IMG_MARGIN), 0);
                imageView.setLayoutParams(ivParams);
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_trash));
                relativeLayout.addView(imageView);
                relativeLayout.bringToFront();
            }
            scheduleView = relativeLayout;
        } else {
            View disableTimeView = new DisableTimeView(context);
            disableTimeView.setLayoutParams(params);
            scheduleView = disableTimeView;
        }

        synchronized (mScheduledViewMap) {
            mScheduledViewMap.put(schedule, scheduleView);
        }
        mTimeTableBoxContainer.addView(scheduleView);
        if (columnCount > 1)
            mCurrentTimeView.bringToFront();
    }

    public void removeSchedule(Schedule schedule) {
        synchronized (mScheduledViewMap) {
            View view = mScheduledViewMap.get(schedule);
            mTimeTableBoxContainer.removeView(view);
            mScheduledViewMap.remove(schedule);
        }
    }

    public void rollbackSchedule() {
        synchronized (mScheduledViewMap) {
            ArrayList<Schedule> removeList = new ArrayList<>();
            for (Schedule exist : mScheduledViewMap.keySet()) {
                if (exist.getType() == Schedule.Type.EDIT) {
                    removeList.add(exist);
                }
            }
            for (Schedule removeData : removeList) {
                mScheduledViewMap.remove(removeData);
            }
        }
    }

    /**
     * set TimeTableView attributes
     *
     * @param attrs attribute
     */
    private void setAttributes(AttributeSet attrs) {
        Context context = getContext();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeTableView);
        rowCount = typedArray.getInt(R.styleable.TimeTableView_row_count, ROW_COUNT);
        int type = typedArray.getInteger(R.styleable.TimeTableView_week_type, TYPE_WEEK);
        columnCount = (type == TYPE_WEEK) ? 7 : 1;
        headerHeight = typedArray.getInt(R.styleable.TimeTableView_header_height, convertDpToPx(HEADER_HEIGHT_DP));
        boxHeight = typedArray.getInt(R.styleable.TimeTableView_box_height, convertDpToPx(BOX_HEIGHT_DP));
        mCurrentBoxHeight = boxHeight;
        timeWidth = typedArray.getInt(R.styleable.TimeTableView_time_width, convertDpToPx(TIME_WIDTH_DP));
        timeTermHour = typedArray.getInt(R.styleable.TimeTableView_time_term_hour, TIME_TERM_HOUR);
        boxColor = typedArray.getColor(R.styleable.TimeTableView_box_color, Color.WHITE);
        boxLineColor = typedArray.getColor(R.styleable.TimeTableView_box_line_color, Color.GRAY);
        boxWidth = getBoxWidth();
        timeMargin = convertDpToPx(TIME_MARGIN_DP);
        scheduleMargin = convertDpToPx((type == TYPE_WEEK) ? 1 : 5);
        typedArray.recycle();
    }

    private void init() {
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                zoomInOut();
                return true;
            }
        });
        setBackgroundColor(boxColor);
        createTableHeader();
        createTimeTableContainer();
    }

    /**
     * control zoom in & out when user two-fingers event.
     * max zoom (scale twice)
     * min zoom (default scale)
     */
    private void zoomInOut() {
        mScaleFactor *= mScaleDetector.getScaleFactor();
        mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 2.0f));
        mCurrentBoxHeight = (int) (boxHeight * mScaleFactor);

        //Resizing table box
        for (TableRow row : mBoxRowList) {
            for (int i = 0; i < row.getChildCount(); i++) {
                row.getChildAt(i).setLayoutParams(getTableRowLayoutParams(mCurrentBoxHeight));
            }
        }
        //Resizing scheduled view
        for (Map.Entry<Schedule, View> entry : mScheduledViewMap.entrySet()) {
            Schedule schedule = entry.getKey();
            View view = entry.getValue();
            view.setLayoutParams(getSharedTimeLayoutParams(schedule));
        }
        //Resizing side time view
        for (int i = 0; i < mSideTimeViewList.size(); i++) {
            mSideTimeViewList.get(i).setLayoutParams(getSideTimeViewParams(i, timeTermHour));
        }
        //change current time view position
        mCurrentTimeView.setLayoutParams(getCurrentTimeViewParams());
    }

    private void createTableHeader() {
        if (columnCount <= 1) {
            return;
        }
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
        final Context context = getContext();
        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mTimeTableBoxContainer = new RelativeLayout(context);
        mTimeTableBoxContainer.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mTimeTableBoxContainer.setBackgroundColor(boxColor);
        mTimeTableBoxContainer.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int pointerCount = event.getPointerCount();
                if (pointerCount == 2) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    mTouchTimeMills = SystemClock.elapsedRealtime();
                    mDrawingSchedule = null;
                    mTouchTime = null;
                } else if (pointerCount == 1) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mTouchTimeMills = SystemClock.elapsedRealtime();
                            final float startX = event.getX();
                            final float startY = event.getY();
                            mDrawingSchedule = getSchedule(startX, startY);
                            if (mDrawingSchedule != null) {
                                mTouchTime = mDrawingSchedule.getEndTime();
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            mIsLongClick = SystemClock.elapsedRealtime() - mTouchTimeMills > LONG_CLICK_TERM;
                            if (mDrawingSchedule != null && mIsLongClick) {
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                final float endY = event.getY();
                                mDrawingSchedule = getSchedule(endY);
                                if (mDrawingView.getVisibility() != View.VISIBLE) {
                                    mDrawingView.setVisibility(View.VISIBLE);
                                }
                                mDrawingView.setLayoutParams(getSharedTimeLayoutParams(mDrawingSchedule));
                                if (columnCount == 1) {
                                    mDrawingView.setText(convertTimeToString(mDrawingSchedule));
                                }
                                mDrawingView.bringToFront();
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (mDrawingSchedule != null) {
                                if (mIsLongClick) {
                                    mDrawingView.setVisibility(View.GONE);
                                    mDrawingSchedule.compareTime();
                                    checkDuplicatedSchedule(mDrawingSchedule, View.GONE);
                                    addSchedule(mDrawingSchedule);
                                    if (mEventListener != null)
                                        mEventListener.onCompleteDraw(mDrawingSchedule);
                                } else {
                                    boolean isClickSchedule = false;
                                    int clickHour = mDrawingSchedule.getStartTime().getHour();
                                    int clickDayOfWeek = mDrawingSchedule.getDayOfWeek();
                                    for (Schedule exist : mScheduledViewMap.keySet()) {
                                        if (clickDayOfWeek == exist.getDayOfWeek() && exist.getStartTime().getHour() < clickHour && exist.getEndTime().getHour() > clickHour) {
                                            if (mEventListener != null)
                                                mEventListener.onClick(exist);
                                            isClickSchedule = true;
                                            break;
                                        }
                                    }

                                    if (!isClickSchedule && mEventListener != null)
                                        mEventListener.onClickTable(findScheduleWithDayOfWeek(mDrawingSchedule));
                                }
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            if (mDrawingView != null) {
                                mDrawingView.setVisibility(View.GONE);
                            }
                            break;
                    }
                }
                mScaleDetector.onTouchEvent(event);
                return true;
            }
        });

        createTableBox();
        createBottomLineView();
        createSideTimeView();
        createCurrentTimeBar();

        mDrawingView = new TextView(context);
        mDrawingView.setLayoutParams(new RelativeLayout.LayoutParams(boxWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        mDrawingView.setBackground(ContextCompat.getDrawable(context, R.drawable.border_schedule_edit_draw));
        mDrawingView.setGravity(Gravity.CENTER);
        mDrawingView.setTextSize(TypedValue.COMPLEX_UNIT_SP, DRAWING_SCHEDULE_TEXT_SIZE);
        mDrawingView.setTextColor(Color.WHITE);
        mDrawingView.setVisibility(View.GONE);
        mTimeTableBoxContainer.addView(mDrawingView);
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
                child.setLayoutParams(getTableRowLayoutParams(mCurrentBoxHeight));
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
        for (int i = 0; i <= rowCount / timeTermHour; i++) {
            TextView textView = new TextView(context);
            textView.setLayoutParams(getSideTimeViewParams(i, timeTermHour));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TIME_TEXT_SIZE);
            textView.setPadding(timeMargin, 0, 0, 0);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            int time = i * timeTermHour;
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
        mCurrentTimeView.setLayoutParams(getCurrentTimeViewParams());
        mCurrentTimeView.setGravity(Gravity.CENTER_VERTICAL);
        View line = new View(context);
        int lineWidth = boxWidth - ((columnCount == 1) ? convertDpToPx(12) : 0);
        line.setLayoutParams(new LinearLayout.LayoutParams(lineWidth, 3));
        line.setBackgroundResource(R.drawable.ic_current_point);
        View point = new View(context);
        point.setLayoutParams(new LinearLayout.LayoutParams(convertDpToPx(7), convertDpToPx(7)));
        point.setBackgroundResource(R.drawable.ic_current_point);
        point.setRotation(45);
        mCurrentTimeView.addView(line);
        mCurrentTimeView.addView(point);
        mTimeTableBoxContainer.addView(mCurrentTimeView);
    }

    private RelativeLayout.LayoutParams getCurrentTimeViewParams() {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int height = convertDpToPx(10);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(boxWidth + height, height);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.setMargins(getMarginLeft(dayOfWeek), calTimePosition(Schedule.getTime(hour, min)), 0, 0);
        return params;
    }

    private RelativeLayout.LayoutParams getSideTimeViewParams(int position, int term) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(timeWidth, (1 + mCurrentBoxHeight) * 2);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.setMargins(0, term * position * (mCurrentBoxHeight + 1), 0, 0);
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
        TableRow.LayoutParams params = new TableRow.LayoutParams(boxWidth, height);
        params.setMargins(0, 0, 1, 1);
        return params;
    }

    private RelativeLayout.LayoutParams getSharedTimeLayoutParams(Schedule schedule) {
        int width = boxWidth - scheduleMargin * 2;
        int height = getSharedTimeBoxHeight(schedule.getStartTime(), schedule.getEndTime());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int leftMargin = scheduleMargin + getMarginLeft(schedule.getDayOfWeek());
        Schedule.Time inputTime;
        if (schedule.getEndTime().isPastOrSameTime(schedule.getStartTime()))
            inputTime = schedule.getEndTime();
        else
            inputTime = schedule.getStartTime();
        int topMargin = convertDpToPx(1) + calTimePosition(inputTime);
        params.setMargins(leftMargin, topMargin, 0, 0);
        return params;
    }

    private int getSharedTimeBoxHeight(Schedule.Time startTime, Schedule.Time endTime) {
        if (startTime.getHour() == endTime.getHour() && startTime.getMin() == endTime.getMin())
            return 0;
        return Math.abs(calTimePosition(endTime) - calTimePosition(startTime)) - convertDpToPx(2);
    }

    private int calTimePosition(Schedule.Time time) {
        int hour = time.getHour();
        int min = time.getMin();
        return hour + mCurrentBoxHeight * (hour + 1) + (mCurrentBoxHeight / 60) * min;
    }

    private int getMarginLeft(int dayOfWeek) {
        return timeWidth + getDayOfWeekPosition(dayOfWeek) * (boxWidth + 1);
    }

    private int getDayOfWeekPosition(int dayOfWeek) {
        if (columnCount == 1) {
            return 0;
        } else if (dayOfWeek == Calendar.SUNDAY) {
            return 6;
        } else {
            return dayOfWeek - 2;
        }
    }

    private int convertPositionToDayOfWeek(int position) {
        if (columnCount == 1) {
            return viewDayOfWeek;
        } else if (position == 6) {
            return Calendar.SUNDAY;
        } else {
            return position + 2;
        }
    }

    private int convertDpToPx(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


    private String convertTimeToString(Schedule schedule) {
        Schedule.Time startTime;
        Schedule.Time endTime;
        if (schedule.getStartTime().isPastOrSameTime(schedule.getEndTime())) {
            startTime = schedule.getStartTime();
            endTime = schedule.getEndTime();
        } else {
            startTime = schedule.getEndTime();
            endTime = schedule.getStartTime();
        }
        return String.format(Locale.getDefault(), "%s:%s ~ %s:%s", checkTimeValue(startTime.getHour()), checkTimeValue(startTime.getMin()), checkTimeValue(endTime.getHour()), checkTimeValue(endTime.getMin()));
    }

    private String checkTimeValue(int value) {
        return ((value < 10) ? "0" : "") + value;
    }

    private int getBoxWidth() {
        Context context = getContext();
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return (size.x - timeWidth) / columnCount;
    }

    private Schedule getSchedule(float x, float y) {
        int dayOfWeek = -1;
        Schedule.Time startTime = null;
        Schedule.Time endTime;
        Schedule.Type type = Schedule.Type.UNSET;

        for (int i = 1; i <= columnCount; i++) {
            int leftLine = timeWidth + i * (boxWidth + 1);
            int diffX = (int) (x - leftLine);
            if (x > timeWidth && diffX < 0) {
                dayOfWeek = convertPositionToDayOfWeek(i - 1);
                break;
            }
        }

        for (int i = 1; i <= rowCount; i++) {
            int topLine = i + mCurrentBoxHeight * (i + 1);
            int diffY = (int) (y - topLine);
            if (diffY < 0) {
                startTime = Schedule.getTime(i - 1, 0);
                break;
            }
        }
        if (dayOfWeek == -1 || startTime == null)
            return null;

        endTime = Schedule.getTime(startTime.getHour() + 1, 0);
        Schedule schedule = new Schedule(startTime, endTime, dayOfWeek, type);
        if (isInDisabledTime(schedule)) {
            return null;
        }

        return schedule;
    }

    private Schedule getSchedule(float y) {
        if (mTouchTime == null)
            return mDrawingSchedule;

        int dayOfWeek = mDrawingSchedule.getDayOfWeek();
        Schedule.Time fingerPositionTime = null;
        Schedule.Type type = Schedule.Type.EDIT;
        for (int i = 0; i <= rowCount; i++) {
            int topLine = i + mCurrentBoxHeight * (i + 1);
            int diffY = (int) (y - topLine);
            if (diffY < 0) {
                fingerPositionTime = Schedule.getTime(i, 0);
                break;
            }
        }
        if (fingerPositionTime == null)
            return mDrawingSchedule;

        Schedule.Time startTime = mDrawingSchedule.getStartTime();
        Schedule.Time endTime = mDrawingSchedule.getEndTime();
        if (fingerPositionTime.isPastTime(mTouchTime)) {
            startTime = fingerPositionTime;
        }
        if (mTouchTime.isPastOrSameTime(fingerPositionTime)) {
            endTime = fingerPositionTime;
        }
        Schedule schedule = new Schedule(startTime, endTime, dayOfWeek, type);
        if (isInDisabledTime(schedule)) {
            return mDrawingSchedule;
        }
        return schedule;
    }

    private List<Schedule> findScheduleWithDayOfWeek(Schedule schedule) {
        ArrayList<Schedule> filterList = new ArrayList<>();
        filterList.add(schedule);
        for (Schedule exist : mScheduledViewMap.keySet()) {
            if (schedule.getDayOfWeek() == exist.getDayOfWeek())
                filterList.add(exist);
        }
        return filterList;
    }

    private void checkDuplicatedSchedule(Schedule schedule, int visibility) {
        synchronized (mScheduledViewMap) {
            ArrayList<Schedule> duplicatedSchedules = new ArrayList<>();
            for (Schedule exist : mScheduledViewMap.keySet()) {
                //case 1: check type schedule was disabled.
                if (exist.getType() == Schedule.Type.DISABLED)
                    continue;

                //case 2: check day of week is same
                if (exist.getDayOfWeek() != schedule.getDayOfWeek())
                    continue;

                //case 3: check time range is duplicated
                if (exist.getEndTime().isPastOrSameTime(schedule.getStartTime()) || schedule.getEndTime().isPastOrSameTime(exist.getStartTime()))
                    continue;

                duplicatedSchedules.add(exist);
            }
            for (Schedule duplicated : duplicatedSchedules) {
                View view = mScheduledViewMap.get(duplicated);
                if (view != null)
                    view.setVisibility(visibility);
            }
        }
    }

    private boolean isInDisabledTime(Schedule schedule) {
        int dayOfWeek = schedule.getDayOfWeek();
        Schedule.Time startTime = schedule.getStartTime();
        Schedule.Time endTime = schedule.getEndTime();
        for (Schedule exist : mScheduledViewMap.keySet()) {
            if (exist.getType() == Schedule.Type.DISABLED && exist.getDayOfWeek() == dayOfWeek) {
                //case 1:  start time to register is in exist time range.
                if (startTime.isPastTime(exist.getEndTime()) && exist.getStartTime().isPastOrSameTime(startTime)) {
                    return true;
                }
                //case 2: end time to register is in exist time range.
                if (endTime.isPastOrSameTime(exist.getEndTime()) && exist.getStartTime().isPastTime(endTime)) {
                    return true;
                }
                //case 3: exist start time is in time to register range.
                if (exist.getStartTime().isPastTime(endTime) && startTime.isPastTime(exist.getStartTime())) {
                    return true;
                }
                //case 4: exist end time is in time to register range.
                if (exist.getEndTime().isPastTime(endTime) && startTime.isPastTime(exist.getEndTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    public interface TimeTableEventListener {
        void onClick(Schedule schedule);

        void onClickTable(List<Schedule> schedule);

        void onCompleteDraw(Schedule schedule);
    }

    private static class DisableTimeView extends View {
        Paint diagonalLine = new Paint();
        Path path = new Path();

        public DisableTimeView(Context context) {
            super(context);
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
}
