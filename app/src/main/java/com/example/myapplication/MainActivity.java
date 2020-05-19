package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.kobbi.view.timetable.Schedule;
import com.kobbi.view.timetable.TimeTableView;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TimeTableView timeTableView = findViewById(R.id.time_table_view);
        Schedule schedule1 = new Schedule(Schedule.getTime(10, 20), Schedule.getTime(19, 20), Calendar.MONDAY, true);
        Schedule schedule2 = new Schedule(Schedule.getTime(20, 0), Schedule.getTime(23, 20), Calendar.MONDAY, true);
        Schedule schedule3 = new Schedule(Schedule.getTime(6, 20), Schedule.getTime(10, 0), Calendar.THURSDAY, false);
        Schedule schedule4 = new Schedule(Schedule.getTime(4, 20), Schedule.getTime(10, 10), Calendar.FRIDAY, false);
        Schedule schedule5 = new Schedule(Schedule.getTime(0, 20), Schedule.getTime(21, 50), Calendar.SATURDAY, true);
        Schedule schedule6 = new Schedule(Schedule.getTime(0, 20), Schedule.getTime(4, 8), Calendar.TUESDAY, true);

        timeTableView.addSchedule(schedule1);
        timeTableView.addSchedule(schedule2);
        timeTableView.addSchedule(schedule3);
        timeTableView.addSchedule(schedule4);
        timeTableView.addSchedule(schedule5);
        timeTableView.addSchedule(schedule6);
    }
}
