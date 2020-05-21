package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.kobbi.view.timetable.Schedule;
import com.kobbi.view.timetable.TimeTableView;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TimeTableView timeTableView = findViewById(R.id.time_table_view);
        Schedule schedule1 = new Schedule(Schedule.getTime(10, 0), Schedule.getTime(19, 0), Calendar.MONDAY, Schedule.Type.ACTIVATED);
        Schedule schedule2 = new Schedule(Schedule.getTime(20, 0), Schedule.getTime(23, 30), Calendar.MONDAY, Schedule.Type.ACTIVATED);
        Schedule schedule4 = new Schedule(Schedule.getTime(4, 0), Schedule.getTime(10, 30), Calendar.FRIDAY, Schedule.Type.ACTIVATED);
        Schedule schedule5 = new Schedule(Schedule.getTime(0, 0), Schedule.getTime(21, 30), Calendar.SATURDAY, Schedule.Type.INACTIVATED);
        Schedule schedule6 = new Schedule(Schedule.getTime(0, 0), Schedule.getTime(4, 30), Calendar.TUESDAY, Schedule.Type.ACTIVATED);
        Schedule schedule7 = new Schedule(Schedule.getTime(0, 0), Schedule.getTime(9, 0), Calendar.WEDNESDAY, Schedule.Type.DISABLED);
        Schedule schedule8 = new Schedule(Schedule.getTime(19, 0), Schedule.getTime(24, 0), Calendar.WEDNESDAY, Schedule.Type.DISABLED);
        Schedule schedule9 = new Schedule(Schedule.getTime(10, 0), Schedule.getTime(18, 0), Calendar.THURSDAY, Schedule.Type.DISABLED);

        timeTableView.addSchedule(schedule1);
        timeTableView.addSchedule(schedule2);
        timeTableView.addSchedule(schedule4);
        timeTableView.addSchedule(schedule5);
        timeTableView.addSchedule(schedule6);
        timeTableView.addSchedule(schedule7);
        timeTableView.addSchedule(schedule8);
        timeTableView.addSchedule(schedule9);

        timeTableView.setEventListener(new TimeTableView.TimeTableEventListener() {
            @Override
            public void onClick(Schedule schedule) {
                Log.e("####", "onClick() --> schedule : " + schedule);
            }

            @Override
            public void onClickTable(List<Schedule> schedules) {
                Log.e("####", "onClickTable() --> schedules : " + schedules);
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("schedules", schedules.toArray(new Schedule[0]));
                startActivity(intent);
            }

            @Override
            public void onCompleteDraw(Schedule schedule) {
                Log.e("####", "onCompleteDraw() --> schedule : " + schedule);
            }
        });
    }
}
