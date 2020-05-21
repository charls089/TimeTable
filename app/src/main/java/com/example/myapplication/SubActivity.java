package com.example.myapplication;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.kobbi.view.timetable.Schedule;
import com.kobbi.view.timetable.TimeTableView;

import java.util.ArrayList;
import java.util.Arrays;

public class SubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        Parcelable[] schedules = getIntent().getParcelableArrayExtra("schedules");
        Log.e("####", "SubActivity.onCreate() --> schedules : " + Arrays.toString(schedules));
        if (schedules != null) {
            TimeTableView timeTableView = findViewById(R.id.time_table_view_day);
            for (Parcelable parcel : schedules) {
                if (parcel instanceof Schedule) {
                    Schedule schedule = (Schedule) parcel;
                    timeTableView.addSchedule(schedule);
                }
            }
        }
    }
}
