package com.utopiaxc.urpassistant.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.utopiaxc.urpassistant.ActivityMain;
import com.utopiaxc.urpassistant.R;
import com.utopiaxc.urpassistant.fuctions.FunctionsPublicBasic;
import com.utopiaxc.urpassistant.sqlite.SQLHelperTimeTable;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.model.Schedule;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class FragmentTimeTableChart extends Fragment {
    private TimetableView timetableView;
    private String handlerMessage = null;
    private static ProgressDialog getTimetableDialog = null;

    //设置菜单UI
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_timetable_menu, menu);
    }

    //菜单栏监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.fragment_timetable_refresh:
                getTimetableDialog = ProgressDialog.show(getActivity(), "获取课表", "请稍候，正在获取课表", true);
                new Thread(new getClasses()).start();
                return true;
            case R.id.fragment_timetable_change_week:
                final Dialog setWeek = new Dialog(getActivity());
                RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.alertdialog_number_picker, null);  //从另外的布局关联组件

                final NumberPicker numberPicker = relativeLayout.findViewById(R.id.numberPicker);
                final Button confirm = relativeLayout.findViewById(R.id.numberPicker_confirm);
                final Button cancel = relativeLayout.findViewById(R.id.numberPicker_cancel);
                numberPicker.setMinValue(1);
                numberPicker.setMaxValue(25);

                setWeek.setTitle(getString(R.string.timetable_layout));
                setWeek.setContentView(relativeLayout);

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setWeek.dismiss();
                        timetableView.changeWeekOnly(numberPicker.getValue());
                        timetableView.hideDateView();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setWeek.dismiss();
                    }
                });

                setWeek.show();

                return true;

            case R.id.fragment_timetable_start_week:
                final AlertDialog.Builder setStartWeek = new AlertDialog.Builder(getActivity());
                LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alertdialog_date_picker, null);

                final DatePicker datePicker = linearLayout.findViewById(R.id.date_picker);

                setStartWeek
                        .setView(linearLayout)
                        .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int year = datePicker.getYear();
                                int month = datePicker.getMonth() + 1;
                                int date = datePicker.getDayOfMonth();

                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TimeTable", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("StartWeek", year + "-" + month + "-" + date);
                                editor.commit();
                                SharedPreferences sharedPreferences_toActivity = getActivity().getSharedPreferences("FirstFragment", getActivity().MODE_PRIVATE);
                                SharedPreferences.Editor editor_toActivity = sharedPreferences_toActivity.edit();
                                editor_toActivity.putInt("Start", 2);
                                editor_toActivity.commit();
                                Intent intent = new Intent(getActivity(), ActivityMain.class);
                                startActivity(intent);
                            }
                        })
                        .create()
                        .show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //FragmentUI创建
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable_chart, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TimeTable", Context.MODE_PRIVATE);
        String start = sharedPreferences.getString("StartWeek", "NULL");
        if (start.equals("NULL")) {
            getActivity().setTitle(getString(R.string.title_table) + "-" + "第1周");
        } else {

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//显示的时间的格式
                Calendar calendar = Calendar.getInstance();
                calendar.setFirstDayOfWeek(2);
                int end_week = calendar.get(Calendar.WEEK_OF_YEAR);


                calendar.setTime(dateFormat.parse(start));
                int start_week = calendar.get(Calendar.WEEK_OF_YEAR);
                int weeks = end_week - start_week + 1;

                if (weeks < 1) {
                    getActivity().setTitle(getString(R.string.title_table) + "-" + "第1周");

                } else {
                    getActivity().setTitle(getString(R.string.title_table) + "-" + "第" + weeks + "周");
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

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

    //设置表内容
    @SuppressLint("Range")
    private void setTimetableView() {
        timetableView = Objects.requireNonNull(getActivity()).findViewById(R.id.id_timetableView);
        SQLHelperTimeTable sqlHelperTimeTable = new SQLHelperTimeTable(getActivity(), "URP_timetable", null, 2);
        SQLiteDatabase sqLiteDatabase = sqlHelperTimeTable.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("classes", new String[]{"ClassName", "Teacher", "Week", "Data", "Count", "School", "Building", "Room", "Time"}, null, null, null, null, null);
        int course_color = 1;
        int flag = 0;
        final List<Schedule> schedules = new ArrayList<>();
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
                    Schedule schedule= new Schedule(course_name, building + room, teacher, list, time, count, day, color_check[i]);
                    schedules.add(schedule);
                    isExist = true;
                    break;
                }
            }

            if (isExist) {
                continue;
            }


            name_check[flag] = course_name;
            color_check[flag] = course_color;
            Schedule schedule = new Schedule(course_name, building + room, teacher, list, time, count, day, course_color++);
            schedules.add(schedule);

            sqLiteDatabase.close();

            //  } catch (Exception e) {
            //      System.out.println(e.toString());
            //       return;
            //   }


        }


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TimeTable", Context.MODE_PRIVATE);
        String start = sharedPreferences.getString("StartWeek", "NULL");
        if (start.equals("NULL")) {
            timetableView.showDateView();
            timetableView.data(schedules)
                    .curWeek(1)
                    .alpha((float)50,(float)0,(float)100)
                    .monthWidthDp(20)
                    .callback(new ISchedule.OnItemClickListener(){
                        @Override
                        public void onItemClick(View v, List<Schedule> scheduleList) {
                            for(Schedule a:scheduleList){
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(a.getName())
                                        .setMessage(a.getTeacher()+"\n"+a.getRoom()+"\n")
                                        .create()
                                        .show();
                            }
                        }
                    })
                    .showView();
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//显示的时间的格式
        try {


            Calendar calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(2);

            int end_week = calendar.get(Calendar.WEEK_OF_YEAR);


            calendar.setTime(dateFormat.parse(start));

            int start_week = calendar.get(Calendar.WEEK_OF_YEAR);


            if (end_week - start_week < 1) {
                timetableView.showDateView();
                timetableView.data(schedules)
                        .curWeek(1)
                        .alpha((float)50,(float)0,(float)100)
                        .monthWidthDp(20)
                        .callback(new ISchedule.OnItemClickListener(){
                            @Override
                            public void onItemClick(View v, List<Schedule> scheduleList) {
                                for(Schedule a:scheduleList){
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle(a.getName())
                                            .setMessage(a.getTeacher()+"\n"+a.getRoom()+"\n")
                                            .create()
                                            .show();
                                }
                            }
                        })
                        .showView();
                return;
            }

            timetableView.showDateView();
            timetableView.data(schedules)
                    .curWeek(end_week - start_week + 1)
                    .alpha((float)50,(float)0,(float)100)
                    .monthWidthDp(20)
                    .callback(new ISchedule.OnItemClickListener(){
                        @Override
                        public void onItemClick(View v, List<Schedule> scheduleList) {
                            for(Schedule a:scheduleList){
                               new AlertDialog.Builder(getActivity())
                                       .setTitle(a.getName())
                                       .setMessage(a.getTeacher()+"\n"+a.getRoom()+"\n")
                                       .create()
                                       .show();
                            }
                        }
                    })

                    .showView();

        } catch (ParseException e) {
            e.printStackTrace();
            timetableView.data(schedules)
                    .curWeek(1)
                    .alpha((float)50,(float)0,(float)100)
                    .monthWidthDp(20)
                    .callback(new ISchedule.OnItemClickListener(){
                        @Override
                        public void onItemClick(View v, List<Schedule> scheduleList) {
                            for(Schedule a:scheduleList){
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(a.getName())
                                        .setMessage(a.getTeacher()+"\n"+a.getRoom()+"\n")
                                        .create()
                                        .show();
                            }
                        }
                    })
                    .showView();
        }
    }

    //获取课程的线程
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

    //异步消息同步
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
                Toast.makeText(getActivity(), "Successful", Toast.LENGTH_LONG);
                setTimetableView();
            }
        }
    };

}
