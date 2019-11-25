package com.utopiaxc.urpassistant.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.utopiaxc.urpassistant.R;
import com.utopiaxc.urpassistant.activities.ActivityExamInfo;
import com.utopiaxc.urpassistant.fuctions.FunctionsPublicBasic;
import com.utopiaxc.urpassistant.sqlite.SQLHelperExamInfo;
import com.utopiaxc.urpassistant.sqlite.SQLHelperTimeTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentAllCourseList extends Fragment {
    private ListView listView;
    private MaterialRefreshLayout refresh;
    private Context context=getActivity();
    private String handerMessgae="";


    //配置FragmentUI
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_center, container, false);
        getActivity().setTitle(getString(R.string.title_table));
        return view;
    }
    //Fragment入口
    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        //预绑定
        listView=getActivity().findViewById(R.id.all_course_list);
        refresh=getActivity().findViewById(R.id.all_course_refresh);

        SharedPreferences sharedPreferences=getActivity().getSharedPreferences("TimeTable", getActivity().MODE_PRIVATE);
        if(!sharedPreferences.getBoolean("ClassIsGot",false))
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.warning)
                    .setMessage(R.string.first_get_grades)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .create()
                    .show();
        else{
            setListView();
        }
        //布局设置
        setRefresh();

    }

    //设置外观
    public void setListView() {
        SQLHelperTimeTable sqlHelperTimeTable = new SQLHelperTimeTable(getActivity(),"URP",null,2);
        SQLiteDatabase sqLiteDatabase=sqlHelperTimeTable.getReadableDatabase();
        List<Map<String, Object>> listitem = new ArrayList<Map<String, Object>>();

        String[] course_name=new String[200];
        String[] attribute=new String[200];
        String[] teacher=new String[200];
        String[] location=new String[200];
        String[] week=new String[200];
        String[] time=new String[200];

        int flag=0;

        Cursor cursor = sqLiteDatabase.query("classes", new String[]{"ClassName","ClassId","Credit","ClassAttribute","ExamAttribute","Teacher","Week","Data","Time","Count","School","Building","Room"}, null, null, null, null, null);
        while(cursor.moveToNext()){
            course_name[flag]=cursor.getString(cursor.getColumnIndex("ClassName"))+"("+cursor.getString(cursor.getColumnIndex("ClassId"))+")";
            attribute[flag]=getActivity().getString(R.string.attribute)+cursor.getString(cursor.getColumnIndex("ClassAttribute"))+" "+cursor.getString(cursor.getColumnIndex("ExamAttribute"))+" "+getActivity().getString(R.string.credit)+cursor.getString(cursor.getColumnIndex("Credit"));
            teacher[flag]=getActivity().getString(R.string.teacher)+cursor.getString(cursor.getColumnIndex("Teacher"));
            location[flag]=getActivity().getString(R.string.room)+cursor.getString(cursor.getColumnIndex("School"))+""+cursor.getString(cursor.getColumnIndex("Building"))+" "+cursor.getString(cursor.getColumnIndex("Room"));
            week[flag]=getActivity().getString(R.string.weeks)+cursor.getString(cursor.getColumnIndex("Week"));
            time[flag]=getActivity().getString(R.string.week_day)+cursor.getString(cursor.getColumnIndex("Data"))+" "+cursor.getString(cursor.getColumnIndex("Time"))+"~"+(Integer.parseInt(cursor.getString(cursor.getColumnIndex("Time")))+Integer.parseInt(cursor.getString(cursor.getColumnIndex("Count")))-1);


            flag++;
        }
        cursor.close();
        sqLiteDatabase.close();


        for (int i = 0; i <flag; i++) {
            Map<String, Object> showitem = new HashMap<String, Object>();

            showitem.put("name", course_name[i]);
            showitem.put("attribute", location[i]);
            showitem.put("teacher", time[i]);
            showitem.put("location", time[i]);
            showitem.put("week", time[i]);
            showitem.put("time", time[i]);
            listitem.add(showitem);
        }



        SimpleAdapter adapter = new SimpleAdapter(context, listitem, R.layout.recycleview_examinfo, new String[]{"name", "attribute", "teacher","location","week","time"}, new int[]{R.id.all_course_card_name, R.id.all_course_card_attribute, R.id.all_course_card_teacher,R.id.all_course_card_location,R.id.all_course_card_weeks,R.id.all_course_card_time});
        listView.setAdapter(adapter);
    }

    //设置刷新
    public void setRefresh() {
        refresh.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
                new Thread(new GetCourse()).start();
            }
        });
    }

    //获取课程信息表
    class GetCourse implements Runnable{

        @Override
        public void run() {

            FunctionsPublicBasic function = new FunctionsPublicBasic();
            SharedPreferences sharedPreferences=getActivity().getSharedPreferences("user",getActivity().MODE_PRIVATE);
            String address=sharedPreferences.getString("address","");
            String username=sharedPreferences.getString("username","");
            String password=sharedPreferences.getString("password","");
            if(function.setClassTableSQL(context,address,username,password)) {
                handerMessgae="over";
                messageHandler.sendMessage(messageHandler.obtainMessage());

            }
            else{
                handerMessgae="fail";
                messageHandler.sendMessage(messageHandler.obtainMessage());
            }
        }
    }

    //异步消息同步
    @SuppressLint("HandlerLeak")
    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (handerMessgae.equals("fail")) {
                refresh.finishRefresh();
                Toast.makeText(context,getString(R.string.refresh_failed),Toast.LENGTH_LONG).show();
            }else{
                setListView();
                refresh.finishRefresh();
                Toast.makeText(context,getString(R.string.refresh_successful),Toast.LENGTH_LONG).show();
            }
        }
    };
}
