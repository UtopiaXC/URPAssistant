package com.utopiaxc.urpassistant.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.utopiaxc.urpassistant.ActivityMain;
import com.utopiaxc.urpassistant.R;

public class ActivityEditor extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("Theme", MODE_PRIVATE);
        int theme = sharedPreferences.getInt("theme", R.style.AppTheme);
        setTheme(theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
    }

    //返回键
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences sharedPreferences_toActivity = getSharedPreferences("FirstFragment", MODE_PRIVATE);
        SharedPreferences.Editor editor_toActivity = sharedPreferences_toActivity.edit();
        editor_toActivity.putInt("Start", 2);
        editor_toActivity.commit();

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, ActivityMain.class);
                startActivity(intent);
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        SharedPreferences sharedPreferences_toActivity = getSharedPreferences("FirstFragment", MODE_PRIVATE);
        SharedPreferences.Editor editor_toActivity = sharedPreferences_toActivity.edit();
        editor_toActivity.putInt("Start", 2);
        editor_toActivity.commit();

        Intent intent = new Intent(this, ActivityMain.class);
        startActivity(intent);
        finish();
    }
}
