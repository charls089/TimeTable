package com.kobbi.view.timetable;

public class Schedule {
    private Time startTime;
    private Time endTime;
    private int dayOfWeek;
    private boolean isEnabled;

    public Schedule(Time startTime, Time endTime, int dayOfWeek, boolean isEnabled) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayOfWeek = dayOfWeek;
        this.isEnabled = isEnabled;
    }

    public static Time getTime(int hour, int min) {
        return new Time(hour, min);
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

    public boolean isEnabled() {
        return isEnabled;
    }

    public static class Time {
        private int hour;
        private int min;

        private Time(int hour, int min) {
            this.hour = hour;
            this.min = min;
        }

        public int getHour() {
            return hour;
        }

        public int getMin() {
            return min;
        }
    }
}
