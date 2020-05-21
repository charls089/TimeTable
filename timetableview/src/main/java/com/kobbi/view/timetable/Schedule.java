package com.kobbi.view.timetable;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Locale;

public class Schedule implements Parcelable {
    private Time startTime;
    private Time endTime;
    private int dayOfWeek;
    private Type type;

    public Schedule(Time startTime, Time endTime, int dayOfWeek, Type type) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayOfWeek = dayOfWeek;
        this.type = type;
    }

    public static Time getTime(int hour, int min) {
        return new Time(hour, min);
    }

    public void compareTime() {
        if (endTime.isPastOrSameTime(startTime)) {
            Time tmpTime = endTime;
            endTime = startTime;
            startTime = tmpTime;
        }
    }

    public void setStartTime(int hour, int min) {
        startTime = new Time(hour, min);
    }

    public void setEndTime(int hour, int min) {
        endTime = new Time(hour, min);
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Time getStartTime() {
        return startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public Type getType() {
        return type;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (startTime != null) {
            sb.append("startTime : [");
            sb.append(String.format(Locale.getDefault(), "%d:%d", startTime.hour, startTime.min));
            sb.append("]");
            sb.append(", ");
        }
        if (endTime != null) {
            sb.append("endTime : [");
            sb.append(String.format(Locale.getDefault(), "%d:%d", endTime.hour, endTime.min));
            sb.append("]");
            sb.append(", ");
        }
        sb.append("dayOfWeek : ");
        sb.append(dayOfWeek);
        sb.append(", ");
        sb.append("type : ");
        sb.append(type);
        sb.append("}");
        return sb.toString();
    }

    private Schedule(Parcel in) {
        startTime = in.readParcelable(Time.class.getClassLoader());
        endTime = in.readParcelable(Time.class.getClassLoader());
        dayOfWeek = in.readInt();
        type = Type.values()[in.readInt()];
    }

    public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {
        @Override
        public Schedule createFromParcel(Parcel in) {
            return new Schedule(in);
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(startTime, flags);
        dest.writeParcelable(endTime, flags);
        dest.writeInt(dayOfWeek);
        dest.writeInt(type.ordinal());
    }

    public static class Time implements Parcelable {
        private int hour;
        private int min;

        private Time(int hour, int min) {
            this.hour = hour;
            this.min = min;
        }

        Time(Parcel in) {
            hour = in.readInt();
            min = in.readInt();
        }

        public static final Creator<Time> CREATOR = new Creator<Time>() {
            @Override
            public Time createFromParcel(Parcel in) {
                return new Time(in);
            }

            @Override
            public Time[] newArray(int size) {
                return new Time[size];
            }
        };

        public int getHour() {
            return hour;
        }

        public int getMin() {
            return min;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(hour);
            dest.writeInt(min);
        }

        @NonNull
        @Override
        public String toString() {
            return "hour : " + hour + ", min : " + min;
        }

        /**
         * Check Time is past or same.
         *
         * @param time compare data.
         * @return Comparing the input time with the existing time, it returns true if the existing time is past or same.
         */
        public boolean isPastOrSameTime(Time time) {
            if (hour < time.getHour()) {
                return true;
            } else if (hour == time.getHour()) {
                return min <= time.getMin();
            }
            return false;
        }

        /**
         * Check Time is past.
         *
         * @param time compare data.
         * @return Comparing the input time with the existing time, it returns true if the existing time is past.
         */
        public boolean isPastTime(Time time) {
            if (hour < time.getHour()) {
                return true;
            } else if (hour == time.getHour()) {
                return min < time.getMin();
            }
            return false;
        }
    }

    public enum Type {
        ACTIVATED,
        INACTIVATED,
        DISABLED,
        EDIT,
        UNSET
    }
}
