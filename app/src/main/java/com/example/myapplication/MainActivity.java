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
        Schedule.Time startTime = Schedule.getTime(0,0);
        Schedule.Time endTime = Schedule.getTime(9,0);

        Schedule.Time startTime2 = Schedule.getTime(19,0);
        Schedule.Time endTime2 = Schedule.getTime(24,0);

        for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
            timeTableView.addSchedule(getSchedule(startTime, endTime, i, Schedule.Type.DISABLED));
            timeTableView.addSchedule(getSchedule(startTime2, endTime2, i, Schedule.Type.DISABLED));
        }

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

    private Schedule getSchedule(Schedule.Time startTime, Schedule.Time endTime, int dayOfWeek, Schedule.Type type) {
        return new Schedule(startTime, endTime, dayOfWeek, type);
    }
}
