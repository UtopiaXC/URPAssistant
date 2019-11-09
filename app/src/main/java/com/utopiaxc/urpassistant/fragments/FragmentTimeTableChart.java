package com.utopiaxc.urpassistant.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.utopiaxc.urpassistant.R;
import com.utopiaxc.urpassistant.fuctions.FunctionsPublicBasic;
import com.utopiaxc.urpassistant.sqlite.SQLHelperGradesList;
import com.utopiaxc.urpassistant.sqlite.SQLHelperTimeTable;
import com.zhuangfei.timetable.core.OnSubjectItemClickListener;
import com.zhuangfei.timetable.core.SubjectBean;
import com.zhuangfei.timetable.core.TimetableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentTimeTableChart extends Fragment {
    private TimetableView timetableView;
    private String handlerMessage = null;
    private static ProgressDialog getTimetableDialog = null;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable_chart, container, false);
        setHasOptionsMenu(true);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TimeTable", getActivity().MODE_PRIVATE);

        if (!sharedPreferences.getBoolean("ClassIsGot", false)) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("No SQL")
                    .setMessage("Get SQL")
                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getTimetableDialog = ProgressDialog.show(getActivity(), "获取课表", "请稍候，正在获取课表", true);
                            new Thread(new getClasses()).start();

                        }
                    })
                    .setNegativeButton("稍后", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create()
                    .show();
        } else {
            setTimetableView();
        }


        return view;
    }

    //Fragment入口
    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TimeTable", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("ClassIsGot", false)) {

        }


        new Thread(new getClasses()).start();


    }

    private void setTimetableView() {
        timetableView = Objects.requireNonNull(getActivity()).findViewById(R.id.id_timetableView);
        SQLHelperTimeTable sqlHelperTimeTable = new SQLHelperTimeTable(getActivity(), "URP", null, 2);
        SQLiteDatabase sqLiteDatabase = sqlHelperTimeTable.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("classes", new String[]{"ClassName", "Teacher", "Week", "Data", "Count", "School", "Building", "Room"}, null, null, null, null, null);
        int course_color = 1;
        List<SubjectBean> subjectBeans = new ArrayList<>();

        while (cursor.moveToNext()) {
            try {
                String course_name = cursor.getString(cursor.getColumnIndex("ClassName"));
                String room = cursor.getString(cursor.getColumnIndex("Room"));
                String week = cursor.getString(cursor.getColumnIndex("Week"));
                String building = cursor.getColumnName(cursor.getColumnIndex("Building"));
                String teacher = cursor.getString(cursor.getColumnIndex("Teacher"));
                int day = Integer.getInteger(cursor.getString(cursor.getColumnIndex("Data")));
                int time = Integer.getInteger(cursor.getString(cursor.getColumnIndex("Time")));
                int count = Integer.getInteger(cursor.getString(cursor.getColumnIndex("Count")));
                List<Integer> list = new ArrayList<>();
                String[] weeks = week.split(",");
                for (int i = 0; i < weeks.length; i++) {
                    weeks[i] = weeks[i].replace(",", "");
                    list.add(Integer.getInteger(weeks[i]));
                }
                SubjectBean subjectBean = new SubjectBean(course_name, building + room, teacher, list, time, count, day, course_color++);
                subjectBeans.add(subjectBean);


            } catch (Exception e) {
                System.out.println(e.toString());
                return;
            }


        }


        timetableView.setDataSource(subjectBeans)
                .setCurWeek(1)
                .setOnSubjectItemClickListener(new OnSubjectItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<SubjectBean> subjectList) {
                        Toast.makeText(getActivity(), "a", Toast.LENGTH_LONG);
                    }
                })
                .showTimetableView();
    }

    class getClasses implements Runnable {

        @Override
        public void run() {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
            String address = sharedPreferences.getString("address", "");
            String username = sharedPreferences.getString("username", "");
            String password = sharedPreferences.getString("password", "");
            FunctionsPublicBasic function = new FunctionsPublicBasic();
            if (!function.setClassTableSQL(getActivity(), address, username, password)) {
                handlerMessage="fail";
                handler.sendMessage(handler.obtainMessage());
            }


        }
    }


    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(handlerMessage.equals("fail")){
                getTimetableDialog.dismiss();
                new AlertDialog.Builder(getActivity())
                        .setTitle("错误！")
                        .setMessage("获取课程表失败")
                        .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create()
                        .show();
                handlerMessage="";
                setTimetableView();
            }else
                getTimetableDialog.dismiss();
        }
    };

}
