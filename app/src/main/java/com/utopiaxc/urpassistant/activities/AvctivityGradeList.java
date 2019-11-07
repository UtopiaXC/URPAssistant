package com.utopiaxc.urpassistant.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.utopiaxc.urpassistant.R;
import com.utopiaxc.urpassistant.fuctions.FunctionsPublicBasic;
import com.utopiaxc.urpassistant.sqlite.SQLHelperGradesList;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvctivityGradeList extends AppCompatActivity {
    private ListView listView;
    private Context context=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences=getSharedPreferences("Theme", MODE_PRIVATE);
        int theme=sharedPreferences.getInt("theme", R.style.AppTheme);
        setTheme(theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gradelist);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        //预绑定
        listView=findViewById(R.id.listview_grade_list);


        if(sharedPreferences.getBoolean("GradeIsGot",false)) {
            Thread gettingGrade = new Thread(new getGrade());
            gettingGrade.start();
            try {
                gettingGrade.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //布局设置
        setListView();

    }

    public void setListView(){
        SQLHelperGradesList sqlHelperGradesList = new SQLHelperGradesList(this,"URP",null,2);
        SQLiteDatabase sqLiteDatabase=sqlHelperGradesList.getReadableDatabase();
        List<Map<String, Object>> listitem = new ArrayList<Map<String, Object>>();

        String[] course_name=new String[200];
        String[] credit=new String[200];
        String[] grades=new String[200];
        int flag=0;

        Cursor cursor = sqLiteDatabase.query("grades", new String[]{"ClassId","ClassName","Credit","Grade"}, null, null, null, null, null);
        while(cursor.moveToNext()){
            course_name[flag]=cursor.getString(cursor.getColumnIndex("ClassName"))+"("+cursor.getString(cursor.getColumnIndex("ClassId"))+")";
            credit[flag]=getString(R.string.credit)+cursor.getString(cursor.getColumnIndex("Credit"));
            grades[flag]=getString(R.string.grade)+cursor.getString(cursor.getColumnIndex("Grade"));
            flag++;
        }
        cursor.close();


        for (int i = 0; i <flag; i++) {
            Map<String, Object> showitem = new HashMap<String, Object>();

            showitem.put("name", course_name[i]);
            showitem.put("credit", credit[i]);
            showitem.put("grade", grades[i]);

            listitem.add(showitem);
        }

        SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), listitem, R.layout.recycleview_gradelist, new String[]{"name", "credit", "grade"}, new int[]{R.id.grades_card_course_name, R.id.grades_card_credit, R.id.grades_card_grade});
        listView.setAdapter(adapter);
    }

    class getGrade implements Runnable{

        @Override
        public void run() {
            FunctionsPublicBasic function = new FunctionsPublicBasic();
            SharedPreferences sharedPreferences=getSharedPreferences("user",MODE_PRIVATE);
            String address=sharedPreferences.getString("address","");
            String username=sharedPreferences.getString("username","");
            String password=sharedPreferences.getString("password","");
            function.setGrades(context,address,username,password);
        }
    }

    //返回键
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
