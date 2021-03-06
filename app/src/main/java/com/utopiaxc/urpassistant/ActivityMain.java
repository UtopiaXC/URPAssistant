package com.utopiaxc.urpassistant;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.utopiaxc.urpassistant.activities.ActivityAbout;
import com.utopiaxc.urpassistant.fragments.FragmentAllCourseList;
import com.utopiaxc.urpassistant.fragments.FragmentCenter;
import com.utopiaxc.urpassistant.fragments.FragmentHome;
import com.utopiaxc.urpassistant.fragments.FragmentTimeTable;
import com.utopiaxc.urpassistant.fragments.FragmentTimeTableChart;
import com.utopiaxc.urpassistant.fuctions.FunctionsPublicBasic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;

public class ActivityMain extends AppCompatActivity {
    private String updateCheak = "";
    SharedPreferences.Editor editor;
    private FunctionsPublicBasic basicFunctions = new FunctionsPublicBasic();


    //底部按钮监听
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    FragmentHome fragmentHome = new FragmentHome();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .replace(R.id.frameLayout, fragmentHome)
                            .commitAllowingStateLoss();
                    return true;

                case R.id.navigation_table:
                    setFragment();
                    return true;
                case R.id.navigation_notifications:
                    FragmentCenter fragmentCenter = new FragmentCenter();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .replace(R.id.frameLayout, fragmentCenter)
                            .commitAllowingStateLoss();
                    return true;
            }
            return false;
        }
    };


    //程序入口
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("Theme", MODE_PRIVATE);
        int theme = sharedPreferences.getInt("theme", R.style.AppTheme);
        setTheme(theme);
        getApplication().setTheme(theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences =this.getSharedPreferences("APP",this.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        boolean isFirst=sharedPreferences.getBoolean("first",true);
        boolean isVersionFirst=false;
        editor.putBoolean("first",false);
        String curVersion="";

        try {
            curVersion=getPackageManager().getPackageInfo(this.getPackageName(),0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
            curVersion="1.0.0";
        }
        String recorded_version=sharedPreferences.getString("version","1.0.0");
        if(!curVersion.equals(recorded_version)){
            isVersionFirst=true;
            editor.putString("version",curVersion);
        }
        editor.commit();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        sharedPreferences =this.getSharedPreferences("user",this.MODE_PRIVATE);
        boolean UserIsSet=sharedPreferences.getBoolean("UserIsSet",false);
        Boolean AddressIsSet = sharedPreferences.getBoolean("AddressIsSet", false);

        //设置主fragment
        sharedPreferences = getSharedPreferences("FirstFragment", MODE_PRIVATE);
        editor = sharedPreferences.edit();


        if(!UserIsSet||!AddressIsSet){
            if(!isFirst) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.warning))
                        .setMessage("您的相关信息未填满")
                        .setPositiveButton(getString(R.string.confirm), null)
                        .create()
                        .show();
            }
            navView.setSelectedItemId(R.id.navigation_notifications);

        }else {
            if (sharedPreferences.getInt("Start", 2) == 1) {
                if (sharedPreferences.getInt("Start_first", 2) == 1) {
                    System.out.println("StartViewByStart1AndStartFirst1");
                    navView.setSelectedItemId(R.id.navigation_home);
                } else if (sharedPreferences.getInt("Start_first", 2) == 2) {
                    System.out.println("StartViewByStart1AndStartFirst2AndMore");
                    navView.setSelectedItemId(R.id.navigation_table);
                }
            } else if (sharedPreferences.getInt("Start", 2) == 2) {
                System.out.println("StartViewByStart2AndMore");
                editor.putInt("Start", 1);
                editor.commit();
                navView.setSelectedItemId(R.id.navigation_table);
            } else if (sharedPreferences.getInt("Start", 2) == 3) {
                System.out.println("StartViewByStart3");
                navView.setSelectedItemId(R.id.navigation_notifications);
                editor.putInt("Start", 1);
                editor.commit();
            }

        }
        if(isVersionFirst){
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.update_log));
            builder.setMessage(getString(R.string.update_log_info));
            builder.setPositiveButton(getText(R.string.confirm), null);
            builder.setCancelable(false);
            builder.show();
        }
        if(isFirst){
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.starting_helper));
            builder.setPositiveButton(getText(R.string.confirm), null);
            builder.setCancelable(false);
            builder.show();
        }
        //开启更新检查线程
        new Thread(new checkupdateRunnable()).start();


    }

    private void setFragment() {
        SharedPreferences sharedPreferences = getSharedPreferences("TimeTable", MODE_PRIVATE);
        if (sharedPreferences.getInt("Layout", 0) == 0) {
            FragmentTimeTable fragmentTimeTable = new FragmentTimeTable();
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.frameLayout, fragmentTimeTable)
                    .commitAllowingStateLoss();
            System.out.println("View0");
        } else if (sharedPreferences.getInt("Layout", 0) == 1) {
            FragmentTimeTableChart fragmentTimeTableChart = new FragmentTimeTableChart();
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.frameLayout, fragmentTimeTableChart)
                    .commitAllowingStateLoss();
            System.out.println("View1");
        } else if (sharedPreferences.getInt("Layout", 0) == 2) {
            FragmentAllCourseList fragmentAllCourseList = new FragmentAllCourseList();
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.frameLayout, fragmentAllCourseList)
                    .commitAllowingStateLoss();
            System.out.println("View2");
        }
    }


    //异步消息同步
    @SuppressLint("HandlerLeak")
    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (updateCheak.equals((String) getText(R.string.has_update))) {
                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
                builder.setTitle(getString(R.string.download_newversion));
                builder.setMessage(getString(R.string.has_update));
                builder.setPositiveButton(getText(R.string.download_newversion), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.utopiaxc.com/Version_Control/URPAssistant_release.apk")));
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { //设定“取消"按钮的功能
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setNeutralButton(getText(R.string.goto_github), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/UtopiaXC/URPAssistant")));
                    }
                });
                builder.show();
            }
        }
    };

    //启动时检查更新
    class checkupdateRunnable implements Runnable {
        @Override
        public void run() {
            try {
                PackageManager packageManager = getPackageManager();
                PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
                String version = packInfo.versionName;


                String latest_version = basicFunctions.getHTML("https://www.utopiaxc.com/Version_Control/URPAssistant_release.txt");


                if (latest_version.equals("error")) {
                    updateCheak = "";
                } else if (latest_version.equals(version)) {
                    updateCheak = "";
                } else {
                    updateCheak = (String) getText(R.string.has_update);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            messageHandler.sendMessage(messageHandler.obtainMessage());
        }
    }

    ;


}
