package com.utopiaxc.urpassistant.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.utopiaxc.urpassistant.R;

import io.github.varenyzc.opensourceaboutpages.AboutPageMessageItem;
import io.github.varenyzc.opensourceaboutpages.MessageCard;

public class ActivitySettings extends AppCompatActivity {
    private MessageCard messageCard;


    //Activity入口
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("Theme", MODE_PRIVATE);
        int theme = sharedPreferences.getInt("theme", R.style.AppTheme);
        setTheme(theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        //预绑定
        messageCard=findViewById(R.id.settings_card);

        //界面设置函数集合
        setMessageCard();

    }

    private void setMessageCard(){
        AboutPageMessageItem ItemSettings_theme=new AboutPageMessageItem(this)
                .setIcon(getDrawable(R.drawable.settings))
                .setMainText(getString(R.string.theme))
                .setOnItemClickListener(new AboutPageMessageItem.AboutPageOnItemClick() {
                    @Override
                    public void onClick() {
                    }
                });
        messageCard.addMessageItem(ItemSettings_theme);

        AboutPageMessageItem ItemSettings_layoutOfTimetable=new AboutPageMessageItem(this)
                .setIcon(getDrawable(R.drawable.settings))
                .setMainText(getString(R.string.timetable_layout))
                .setOnItemClickListener(new AboutPageMessageItem.AboutPageOnItemClick() {
                    @Override
                    public void onClick() {
                    }
                });
        messageCard.addMessageItem(ItemSettings_layoutOfTimetable);

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
