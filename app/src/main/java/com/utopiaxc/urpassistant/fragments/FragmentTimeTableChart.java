package com.utopiaxc.urpassistant.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
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
import android.widget.EditText;
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
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.view.WeekView;

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
    private TextView textView_frount;
    private TextView textView_now;
    private TextView textView_next;

    //设置菜单UI
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TempWeek", getActivity().MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isCurWeek", false);
                        editor.putInt("Week", numberPicker.getValue());
                        editor.commit();
                        SharedPreferences sharedPreferences_toActivity = getActivity().getSharedPreferences("FirstFragment", getActivity().MODE_PRIVATE);
                        SharedPreferences.Editor editor_toActivity = sharedPreferences_toActivity.edit();
                        editor_toActivity.putInt("Start", 2);
                        editor_toActivity.commit();
                        Intent intent = new Intent(getActivity(), ActivityMain.class);
                        startActivity(intent);

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

        SharedPreferences sharedPreferences_curWeek = getActivity().getSharedPreferences("TempWeek", getActivity().MODE_PRIVATE);
        boolean tempWeek = sharedPreferences_curWeek.getBoolean("isCurWeek", true);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TimeTable", getActivity().MODE_PRIVATE);
        String start = sharedPreferences.getString("StartWeek", "NULL");
        if (tempWeek) {
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
        } else {
            int weeks = sharedPreferences_curWeek.getInt("Week", 1);
            System.out.println("Test" + weeks);
            getActivity().setTitle(getString(R.string.title_table) + "-" + "第" + weeks + "周");
        }
        System.out.println("SetView");
        return view;
    }

    //Fragment入口
    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TimeTable", getActivity().MODE_PRIVATE);

        setTextViewButton();

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

    //设置切换周按钮
    private void setTextViewButton() {
        textView_frount = getActivity().findViewById(R.id.textView_frount);
        textView_now = getActivity().findViewById(R.id.textView_now);
        textView_next = getActivity().findViewById(R.id.textView_next);

        textView_frount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cur = timetableView.curWeek();
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TempWeek", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isCurWeek", false);
                editor.putInt("Week", cur - 1);
                editor.commit();
                SharedPreferences sharedPreferences_toActivity = getActivity().getSharedPreferences("FirstFragment", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor_toActivity = sharedPreferences_toActivity.edit();
                editor_toActivity.putInt("Start", 2);
                editor_toActivity.commit();
                Intent intent = new Intent(getActivity(), ActivityMain.class);
                startActivity(intent);
            }
        });

        textView_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences_toActivity = getActivity().getSharedPreferences("FirstFragment", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor_toActivity = sharedPreferences_toActivity.edit();
                editor_toActivity.putInt("Start", 2);
                editor_toActivity.commit();
                Intent intent = new Intent(getActivity(), ActivityMain.class);
                startActivity(intent);
            }
        });

        textView_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cur = timetableView.curWeek();
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TempWeek", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isCurWeek", false);
                editor.putInt("Week", cur + 1);
                editor.commit();
                SharedPreferences sharedPreferences_toActivity = getActivity().getSharedPreferences("FirstFragment", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor_toActivity = sharedPreferences_toActivity.edit();
                editor_toActivity.putInt("Start", 2);
                editor_toActivity.commit();
                Intent intent = new Intent(getActivity(), ActivityMain.class);
                startActivity(intent);
            }
        });

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
                    Schedule schedule = new Schedule(course_name, building + room, teacher, list, time, count, day, color_check[i]);
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

        timetableView.showDateView();
        timetableView.data(schedules)
                .alpha((float) 50, (float) 0, (float) 100)
                .monthWidthDp(20)
                .callback(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<Schedule> scheduleList) {
                        for (Schedule item : scheduleList) {
                            setItemOnClickListener(item);
                        }
                    }
                });

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TimeTable", Context.MODE_PRIVATE);
        String start = sharedPreferences.getString("StartWeek", "NULL");
        SharedPreferences sharedPreferences_curWeek = getActivity().getSharedPreferences("TempWeek", getActivity().MODE_PRIVATE);
        boolean curWeek = sharedPreferences_curWeek.getBoolean("isCurWeek", true);
        if (start.equals("NULL")) {
            timetableView.curWeek(1)
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
                timetableView.curWeek(1)
                        .showView();
                return;
            }
            timetableView.curWeek(end_week - start_week + 1)
                    .showView();

        } catch (ParseException e) {
            e.printStackTrace();
            timetableView.curWeek(1)
                    .showView();
        }
        if (!curWeek) {
            int cur = timetableView.curWeek();
            int to_week = sharedPreferences_curWeek.getInt("Week", 1);
            //更新切换后的日期，从当前周cur->切换的周week
            timetableView.onDateBuildListener()
                    .onUpdateDate(cur, to_week);
            timetableView.changeWeekForce(to_week);
            SharedPreferences.Editor editor = sharedPreferences_curWeek.edit();
            editor.putBoolean("isCurWeek", true);
            editor.commit();
        }
    }

    //设置课程点击监听
    @SuppressLint("SetTextI18n")
    private void setItemOnClickListener(Schedule item) {

        SQLHelperTimeTable sqlHelperTimeTable = new SQLHelperTimeTable(getActivity(), "URP_timetable", null, 2);
        SQLiteDatabase sqLiteDatabase = sqlHelperTimeTable.getReadableDatabase();
        String selection_name = item.getName();
        String selection_data = String.valueOf(item.getDay());
        Cursor cursor = sqLiteDatabase.query("classes", new String[]{"ClassName", "ClassId", "Credit", "ClassAttribute", "ExamAttribute", "Teacher", "Week", "Data", "Count", "School", "Building", "Room", "Time"}, "ClassName=? and Data=?", new String[]{selection_name, selection_data}, null, null, null);

        android.app.AlertDialog.Builder CourseMessage = new android.app.AlertDialog.Builder(getActivity());
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alertdialog_course_message, null);  //从另外的布局关联组件
        TextView textView_name = linearLayout.findViewById(R.id.alertdialog_course_message_name);
        TextView textView_id = linearLayout.findViewById(R.id.alertdialog_course_message_id);
        TextView textView_credit = linearLayout.findViewById(R.id.alertdialog_course_message_credit);
        TextView textView_attribute = linearLayout.findViewById(R.id.alertdialog_course_message_attribute);
        TextView textView_examattribute = linearLayout.findViewById(R.id.alertdialog_course_message_examAttribute);
        TextView textView_week = linearLayout.findViewById(R.id.alertdialog_course_message_week);
        TextView textView_time = linearLayout.findViewById(R.id.alertdialog_course_message_time);
        TextView textView_teacher = linearLayout.findViewById(R.id.alertdialog_course_message_teacher);
        TextView textView_school = linearLayout.findViewById(R.id.alertdialog_course_message_school);
        TextView textView_room = linearLayout.findViewById(R.id.alertdialog_course_message_room);
        while (cursor.moveToNext()) {
            textView_name.setText(getActivity().getText(R.string.course_name)+cursor.getString(cursor.getColumnIndex("ClassName")));
            textView_id.setText(getActivity().getText(R.string.course_id)+cursor.getString(cursor.getColumnIndex("ClassId")));
            textView_credit.setText(getActivity().getText(R.string.credit)+cursor.getString(cursor.getColumnIndex("Credit")));
            textView_attribute.setText(getActivity().getText(R.string.course_attribute)+cursor.getString(cursor.getColumnIndex("ClassAttribute")));
            textView_examattribute.setText(getActivity().getText(R.string.exam_attribute)+cursor.getString(cursor.getColumnIndex("ExamAttribute")));
            textView_week.setText(getActivity().getText(R.string.weeks)+cursor.getString(cursor.getColumnIndex("Week")));
            int count=Integer.valueOf(cursor.getString(cursor.getColumnIndex("Count")));
            int start=Integer.valueOf(cursor.getString(cursor.getColumnIndex("Time")));
            textView_time.setText(getActivity().getText(R.string.course_time)+String.valueOf(start)+"~"+String.valueOf(start+count-1));
            textView_teacher.setText(getActivity().getText(R.string.teacher)+cursor.getString(cursor.getColumnIndex("Teacher")));
            textView_school.setText(getActivity().getText(R.string.school)+cursor.getString(cursor.getColumnIndex("School")));
            textView_room.setText(getActivity().getText(R.string.room)+cursor.getString(cursor.getColumnIndex("Building"))+cursor.getString(cursor.getColumnIndex("Room")));
            break;
        }
        CourseMessage.setView(linearLayout)
                .setPositiveButton(getActivity().getText(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton(getActivity().getText(R.string.edit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setEditView(item.getName(),item.getDay());
                    }
                })
                .create()
                .show();
    }

    //设置课程编辑框
    private void setEditView(String name,int day){

        SQLHelperTimeTable sqlHelperTimeTable = new SQLHelperTimeTable(getActivity(), "URP_timetable", null, 2);
        SQLiteDatabase sqLiteDatabase = sqlHelperTimeTable.getReadableDatabase();
        String selection_name = name;
        String selection_data = String.valueOf(day);
        Cursor cursor = sqLiteDatabase.query("classes", new String[]{"ClassName", "ClassId", "Credit", "ClassAttribute", "ExamAttribute", "Teacher", "Week", "Data", "Count", "School", "Building", "Room", "Time"}, "ClassName=? and Data=?", new String[]{selection_name, selection_data}, null, null, null);


        android.app.AlertDialog.Builder CourseEditor = new android.app.AlertDialog.Builder(getActivity());
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alertdialog_course_editor, null);  //从另外的布局关联组件
        EditText textView_name = linearLayout.findViewById(R.id.alertdialog_course_message_editText_name);
        EditText textView_id = linearLayout.findViewById(R.id.alertdialog_course_message_editText_id);
        EditText textView_credit = linearLayout.findViewById(R.id.alertdialog_course_message_editText_credit);
        EditText textView_attribute = linearLayout.findViewById(R.id.alertdialog_course_message_editText_attribute);
        EditText textView_examattribute = linearLayout.findViewById(R.id.alertdialog_course_message_editText_examAttribute);
        EditText textView_teacher = linearLayout.findViewById(R.id.alertdialog_course_message_editText_teacher);
        EditText textView_school = linearLayout.findViewById(R.id.alertdialog_course_message_editText_school);
        EditText textView_room = linearLayout.findViewById(R.id.alertdialog_course_message_editText_room);
        EditText textView_week = linearLayout.findViewById(R.id.alertdialog_course_message_editText_week);
        EditText textView_time = linearLayout.findViewById(R.id.alertdialog_course_message_editText_time);

        String courseName="";
        String courseData="";


        while(cursor.moveToNext()) {
            textView_name.setHint(cursor.getString(cursor.getColumnIndex("ClassName")));
            courseName=cursor.getString(cursor.getColumnIndex("ClassName"));
            courseData=cursor.getString(cursor.getColumnIndex("Data"));
            textView_id.setHint(cursor.getString(cursor.getColumnIndex("ClassId")));
            textView_credit.setHint(cursor.getString(cursor.getColumnIndex("Credit")));
            textView_attribute.setHint(cursor.getString(cursor.getColumnIndex("ClassAttribute")));
            textView_examattribute.setHint(cursor.getString(cursor.getColumnIndex("ExamAttribute")));
            textView_teacher.setHint(cursor.getString(cursor.getColumnIndex("Teacher")));
            textView_school.setHint(cursor.getString(cursor.getColumnIndex("School")));
            textView_room.setHint(cursor.getString(cursor.getColumnIndex("Building"))+cursor.getString(cursor.getColumnIndex("Room")));
            break;
        }

        CourseEditor.setCancelable(false);
        CourseEditor.setView(linearLayout)
                .setPositiveButton(getActivity().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SQLHelperTimeTable sqlHelperTimeTable = new SQLHelperTimeTable(getActivity(), "URP_timetable", null, 2);
                        SQLiteDatabase sqLiteDatabase = sqlHelperTimeTable.getWritableDatabase();
                        if(!textView_name.getText().toString().equals("")){
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("ClassName",textView_name.getText().toString());
                            sqLiteDatabase.update("classes",
                                    contentValues,
                                    "ClassName = ?",
                                    new String[]{name});
                        }

                        if(!textView_id.getText().toString().equals("")){
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("ClassId",textView_id.getText().toString());
                            sqLiteDatabase.update("classes",
                                    contentValues,
                                    "ClassName = ?",
                                    new String[]{name});
                        }

                        if(!textView_credit.getText().toString().equals("")){
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("Credit",textView_credit.getText().toString());
                            sqLiteDatabase.update("classes",
                                    contentValues,
                                    "ClassName = ?",
                                    new String[]{name});
                        }

                        if(!textView_attribute.getText().toString().equals("")){
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("ClassAttribute",textView_attribute.getText().toString());
                            sqLiteDatabase.update("classes",
                                    contentValues,
                                    "ClassName = ?",
                                    new String[]{name});
                        }

                        if(!textView_examattribute.getText().toString().equals("")){
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("ExamAttribute",textView_examattribute.getText().toString());
                            sqLiteDatabase.update("classes",
                                    contentValues,
                                    "ClassName = ?",
                                    new String[]{name});
                        }

                        if(!textView_week.getText().toString().equals("")){

                        }

                        if(!textView_time.getText().toString().equals("")){

                        }

                        if(!textView_teacher.getText().toString().equals("")){
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("Teacher",textView_teacher.getText().toString());
                            sqLiteDatabase.update("classes",
                                    contentValues,
                                    "ClassName = ?",
                                    new String[]{name});
                        }

                        if(!textView_school.getText().toString().equals("")){
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("School",textView_school.getText().toString());
                            sqLiteDatabase.update("classes",
                                    contentValues,
                                    "ClassName = ?",
                                    new String[]{name});
                        }

                        if(!textView_teacher.getText().toString().equals("")){
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("Teacher",textView_teacher.getText().toString());
                            sqLiteDatabase.update("classes",
                                    contentValues,
                                    "ClassName = ?",
                                    new String[]{name});
                        }

                        if(!textView_room.getText().toString().equals("")){
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("Room",textView_room.getText().toString());
                            contentValues.put("Building","");
                            sqLiteDatabase.update("classes",
                                    contentValues,
                                    "ClassName = ?",
                                    new String[]{name});
                        }



















                        setTimetableView();
                    }
                })
                .setNegativeButton(getActivity().getString(R.string.cancel),null)
                .create()
                .show();
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
