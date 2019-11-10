package com.utopiaxc.urpassistant.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_timetable_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.fragment_timetable_refresh:
                getTimetableDialog = ProgressDialog.show(getActivity(), "获取课表", "请稍候，正在获取课表", true);
                new Thread(new getClasses()).start();
                return true;
            case R.id.fragment_timetable_change_week:
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TimeTable", getActivity().MODE_PRIVATE);
                final SharedPreferences.Editor editor=sharedPreferences.edit();

                final Dialog setWeek = new Dialog(getActivity());
                RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.alertdialog_number_picker, null);  //从另外的布局关联组件

                final NumberPicker numberPicker = relativeLayout.findViewById(R.id.numberPicker);
                final Button confirm = relativeLayout.findViewById(R.id.numberPicker_confirm);
                final Button cancel =relativeLayout.findViewById(R.id.numberPicker_cancel);
                numberPicker.setMinValue(1);
                numberPicker.setMaxValue(25);

                setWeek.setTitle(getString(R.string.timetable_layout));
                setWeek.setContentView(relativeLayout);

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editor.putInt("Week",numberPicker.getValue());
                        editor.commit();
                        setWeek.dismiss();
                        timetableView.changeWeek(numberPicker.getValue(),true);
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setWeek.dismiss();
                    }
                });

                setWeek.show();


            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable_chart, container, false);
        return view;
    }

    //Fragment入口
    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TimeTable", getActivity().MODE_PRIVATE);

        if (!sharedPreferences.getBoolean("ClassIsGot", false)) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.no_timetable_sql))
                    .setMessage(getString(R.string.get_timetable_sql))
                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getTimetableDialog = ProgressDialog.show(getActivity(), getString(R.string.alert), getString(R.string.getting_timetable), true);
                            new Thread(new getClasses()).start();

                        }
                    })
                    .setNegativeButton(getString(R.string.later), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create()
                    .show();
        } else {
            setTimetableView();
        }


    }

    private void setTimetableView() {
        timetableView = Objects.requireNonNull(getActivity()).findViewById(R.id.id_timetableView);
        SQLHelperTimeTable sqlHelperTimeTable = new SQLHelperTimeTable(getActivity(), "URP_timetable", null, 2);
        SQLiteDatabase sqLiteDatabase = sqlHelperTimeTable.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("classes", new String[]{"ClassName", "Teacher", "Week", "Data", "Count", "School", "Building", "Room", "Time"}, null, null, null, null, null);
        int course_color = 1;
        int flag = 0;
        List<SubjectBean> subjectBeans = new ArrayList<>();
        String[] name_check = new String[40];
        int[] color_check = new int[40];

        while (cursor.moveToNext()) {
            // try {
            String course_name = cursor.getString(cursor.getColumnIndex("ClassName"));
            String room = cursor.getString(cursor.getColumnIndex("Room"));
            String week = cursor.getString(cursor.getColumnIndex("Week"));
            String building = cursor.getString(cursor.getColumnIndex("Building"));
            String teacher = cursor.getString(cursor.getColumnIndex("Teacher"));
            if (cursor.getString(cursor.getColumnIndex("Data")).equals("^") || cursor.getString(cursor.getColumnIndex("Time")).equals("^") || cursor.getString(cursor.getColumnIndex("Count")).equals("^")) {
                continue;
            }


            int day = Integer.parseInt(cursor.getString(cursor.getColumnIndex("Data")));
            int time = Integer.parseInt(cursor.getString(cursor.getColumnIndex("Time")));
            int count = Integer.parseInt(cursor.getString(cursor.getColumnIndex("Count")));
            List<Integer> list = new ArrayList<>();
            String[] weeks = week.split(",");
            for (int i = 0; i < weeks.length; i++) {
                weeks[i] = weeks[i].replace(",", "");
                list.add(Integer.parseInt(weeks[i]));
            }

            boolean isExist = false;

            for (int i = 0; i < name_check.length; i++) {
                if (course_name.equals(name_check[i])) {
                    SubjectBean subjectBean = new SubjectBean(course_name, building + room, teacher, list, time, count, day, color_check[i]);
                    subjectBeans.add(subjectBean);
                    isExist = true;
                    break;
                }
            }

            if (isExist) {
                continue;
            }


            name_check[flag] = course_name;
            color_check[flag] = course_color;
            SubjectBean subjectBean = new SubjectBean(course_name, building + room, teacher, list, time, count, day, course_color++);
            subjectBeans.add(subjectBean);


            //  } catch (Exception e) {
            //      System.out.println(e.toString());
            //       return;
            //   }


        }


        SharedPreferences sharedPreferences=getActivity().getSharedPreferences("TimeTable",Context.MODE_PRIVATE);
        int week=sharedPreferences.getInt("Week",1);

        timetableView.setDataSource(subjectBeans)
                .setCurWeek(week)
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
                handlerMessage = "fail";
                handler.sendMessage(handler.obtainMessage());
            } else {
                handlerMessage = "success";
                handler.sendMessage(handler.obtainMessage());
            }


        }
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("ShowToast")
        @Override
        public void handleMessage(Message msg) {
            if (handlerMessage.equals("fail")) {
                getTimetableDialog.dismiss();
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.error))
                        .setMessage(getString(R.string.fail_to_get_timetable))
                        .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create()
                        .show();
                handlerMessage = "";

            } else {
                handlerMessage = "";
                getTimetableDialog.dismiss();
                Toast.makeText(getActivity(),"Successful",Toast.LENGTH_LONG);
                setTimetableView();
            }
        }
    };

}
