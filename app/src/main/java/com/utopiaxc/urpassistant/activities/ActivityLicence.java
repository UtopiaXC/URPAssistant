package com.utopiaxc.urpassistant.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.utopiaxc.urpassistant.R;

import io.github.varenyzc.opensourceaboutpages.AboutPageMessageItem;
import io.github.varenyzc.opensourceaboutpages.MessageCard;

public class ActivityLicence extends AppCompatActivity {
    private MessageCard messageCard;

    //Activity入口
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        messageCard=findViewById(R.id.licences);
        setMessageCard();

    }

    private void setMessageCard(){
        AboutPageMessageItem ItemLicence_AboutPage=new AboutPageMessageItem(this)
                .setIcon(getDrawable(R.drawable.developer))
                .setMainText("Open Source About Page")
                .setDescriptionText("Powered By varenyzc")
                .setOnItemClickListener(new AboutPageMessageItem.AboutPageOnItemClick() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent();
                        //Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse("https://github.com/varenyzc/OpenSourceAboutPage/blob/master/LICENSE");
                        intent.setData(content_url);
                        startActivity(intent);
                    }
                });
        messageCard.addMessageItem(ItemLicence_AboutPage);
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
