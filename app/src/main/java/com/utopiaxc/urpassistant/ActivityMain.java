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
import com.utopiaxc.urpassistant.fragments.FragmentCenter;
import com.utopiaxc.urpassistant.fragments.FragmentHome;
import com.utopiaxc.urpassistant.fragments.FragmentTimeTable;
import com.utopiaxc.urpassistant.fuctions.FunctionsPublicBasic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;

public class ActivityMain extends AppCompatActivity {

    private FragmentHome fragmentHome;
    private FragmentTimeTable fragmentTimeTable;
    private FragmentCenter fragmentCenter;
    private String updateCheak="";
    private FunctionsPublicBasic basicFunctions = new FunctionsPublicBasic();

    //底部按钮监听
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragmentHome=new FragmentHome();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .replace(R.id.frameLayout, fragmentHome)
                            .commitAllowingStateLoss();
                    return true;

                case R.id.navigation_table:
                    fragmentTimeTable=new FragmentTimeTable();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .replace(R.id.frameLayout, fragmentTimeTable)
                            .commitAllowingStateLoss();
                    return true;
                case R.id.navigation_notifications:
                    fragmentCenter=new FragmentCenter();
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
        SharedPreferences sharedPreferences=getSharedPreferences("Theme",MODE_PRIVATE);
        int theme=sharedPreferences.getInt("theme",R.style.AppTheme);
        setTheme(theme);
        getApplication().setTheme(theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //设置主fragment
        fragmentHome = new FragmentHome();
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .add(R.id.frameLayout, fragmentHome)
                .commitAllowingStateLoss();

        //开启更新检查线程
        new Thread(new checkupdateRunnable()).start();

    }



    //异步消息同步
    @SuppressLint("HandlerLeak")
    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (updateCheak.equals((String)getText(R.string.has_update))) {
                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
                builder.setTitle(getString(R.string.download_newversion));
                builder.setMessage(getString(R.string.has_update));
                builder.setPositiveButton(getText(R.string.download_newversion), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.utopiaxc.com/Version_Control/URPAssistant_debug.apk")));
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


                String latest_version = basicFunctions.getHTML("http://www.utopiaxc.com/Version_Control/URPAssistant_debug.txt");


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
    };


}
