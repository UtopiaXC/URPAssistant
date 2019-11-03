package com.utopiaxc.urpassistant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.utopiaxc.urpassistant.R;
import com.utopiaxc.urpassistant.activities.ActivityAbout;

import io.github.varenyzc.opensourceaboutpages.AboutPageMessageItem;
import io.github.varenyzc.opensourceaboutpages.MessageCard;

public class FragmentCenter extends Fragment {
    private MessageCard selectionCenter;


    //配置FragmentUI
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_center, container, false);

        return view;
    }

    //Fragment入口
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //预绑定
        selectionCenter=getActivity().findViewById(R.id.center_selectionCard);


        //预设及监听
        setSelectionCenter();
    }

    //设置中心选框
    private void setSelectionCenter(){

        //添加设置选框
        AboutPageMessageItem ItemSelectionCenter_settings=new AboutPageMessageItem(getActivity())
                .setIcon(getActivity().getDrawable(R.drawable.settings))
                .setMainText(getString(R.string.settings))
                .setOnItemClickListener(new AboutPageMessageItem.AboutPageOnItemClick() {
                    @Override
                    public void onClick() {

                    }
                });
        selectionCenter.addMessageItem(ItemSelectionCenter_settings);

        //添加关于选框
        AboutPageMessageItem ItemSelectionCenter_about=new AboutPageMessageItem(getActivity())
                .setIcon(getActivity().getDrawable(R.drawable.information))
                .setMainText(getString(R.string.about))
                .setOnItemClickListener(new AboutPageMessageItem.AboutPageOnItemClick() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(getActivity(), ActivityAbout.class);
                        startActivity(intent);
                    }
                });
        selectionCenter.addMessageItem(ItemSelectionCenter_about);

    }
}
