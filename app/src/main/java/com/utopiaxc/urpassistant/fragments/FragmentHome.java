package com.utopiaxc.urpassistant.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.utopiaxc.urpassistant.R;
import com.utopiaxc.urpassistant.activities.ActivitySettings;
import com.utopiaxc.urpassistant.activities.AvctivityGradeList;
import com.utopiaxc.urpassistant.fuctions.FunctionsPublicBasic;

import io.github.varenyzc.opensourceaboutpages.AboutPageMessageItem;
import io.github.varenyzc.opensourceaboutpages.MessageCard;

public class FragmentHome extends Fragment {
    private MessageCard messageCard;


    //配置FragmentUI
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        return view;
    }

    //Fragment入口
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //预绑定
        messageCard=getActivity().findViewById(R.id.home_card);

        //设置卡片
        setMessageCard();

    }

    //卡片设置函数
    private void setMessageCard(){
        AboutPageMessageItem ItemHome_getGrades=new AboutPageMessageItem(getActivity())
                .setIcon(getActivity().getDrawable(R.drawable.gradelist))
                .setMainText(getString(R.string.grade_list))
                .setOnItemClickListener(new AboutPageMessageItem.AboutPageOnItemClick() {
                    @Override
                    public void onClick() {
                        Intent intent=new Intent(getActivity(), AvctivityGradeList.class);
                        startActivity(intent);
                    }
                });
        messageCard.addMessageItem(ItemHome_getGrades);
    }

}
