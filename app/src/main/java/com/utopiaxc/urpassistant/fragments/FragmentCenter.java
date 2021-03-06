package com.utopiaxc.urpassistant.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.utopiaxc.urpassistant.R;
import com.utopiaxc.urpassistant.activities.ActivityAbout;
import com.utopiaxc.urpassistant.activities.ActivitySettings;
import com.utopiaxc.urpassistant.fuctions.FunctionsPublicBasic;

import io.github.varenyzc.opensourceaboutpages.AboutPageMessageItem;
import io.github.varenyzc.opensourceaboutpages.MessageCard;

public class FragmentCenter extends Fragment {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private MessageCard selectionCenter;
    private MessageCard userCenter;
    private FunctionsPublicBasic functions = new FunctionsPublicBasic();
    private boolean isUseful;
    private static ProgressDialog testDialog = null;
    private Context context=getActivity();
    boolean ClassIsGot=false;
    boolean ExamIsGot=false;
    boolean GradeIsGot=false;

    //配置FragmentUI
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_center, container, false);
        getActivity().setTitle(getString(R.string.title_center));
        return view;
    }

    //Fragment入口
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences("user", getActivity().MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //预绑定
        selectionCenter = getActivity().findViewById(R.id.center_selectionCard);
        userCenter = getActivity().findViewById(R.id.center_userCard);

        //预设及监听
        setUserCenter();
        setSelectionCenter();

    }

    //设置个人中心选框
    private void setUserCenter() {
        //添加账户选框
        AboutPageMessageItem ItemUserCard_user = new AboutPageMessageItem(getActivity())
                .setIcon(getActivity().getDrawable(R.drawable.account))
                .setMainText(getString(R.string.user_message))
                .setOnItemClickListener(new AboutPageMessageItem.AboutPageOnItemClick() {
                    @Override
                    public void onClick() {
                        final AlertDialog.Builder setAccount = new AlertDialog.Builder(getActivity());
                        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alertdialog_account, null);  //从另外的布局关联组件
                        final EditText login_name = linearLayout.findViewById(R.id.login_username);
                        final EditText login_password = linearLayout.findViewById(R.id.login_password);
                        Boolean isSet = sharedPreferences.getBoolean("UserIsSet", false);
                        setAccount.setTitle(getString(R.string.urp_account));
                        if (isSet.equals(true))
                            setAccount.setTitle(getActivity().getString(R.string.urp_account) + getActivity().getString(R.string.configured));
                        setAccount.setView(linearLayout)
                                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String username = login_name.getText().toString();
                                        String password = login_password.getText().toString();
                                        if (!username.equals("") && !password.equals("")) {
                                            editor.putString("username", username);
                                            editor.putString("password", password);
                                            editor.putBoolean("UserIsSet", true);
                                            editor.commit();
                                        }
                                    }
                                })
                                .create()
                                .show();

                    }
                });
        userCenter.addMessageItem(ItemUserCard_user);

        //添加地址选框
        AboutPageMessageItem ItemUserCard_address = new AboutPageMessageItem(getActivity())
                .setIcon(getActivity().getDrawable(R.drawable.address))
                .setMainText(getString(R.string.urp_adress))
                .setOnItemClickListener(new AboutPageMessageItem.AboutPageOnItemClick() {
                    @Override
                    public void onClick() {
                        final AlertDialog.Builder setURP = new AlertDialog.Builder(getActivity());
                        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alertdialog_address, null);  //从另外的布局关联组件
                        final EditText address = linearLayout.findViewById(R.id.urp_address);
                        Boolean isSet = sharedPreferences.getBoolean("AddressIsSet", false);
                        setURP.setTitle(getString(R.string.urp_account));
                        if (isSet.equals(true))
                            setURP.setTitle(getActivity().getString(R.string.urp_adress) + getActivity().getString(R.string.configured));
                        setURP.setMessage(getActivity().getString(R.string.address_alert))
                                .setView(linearLayout)
                                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String string_address = address.getText().toString();
                                        if (!string_address.equals("")) {
                                            editor.putString("address", string_address);
                                            editor.putBoolean("AddressIsSet", true);
                                            editor.commit();
                                        }
                                    }
                                })
                                .create()
                                .show();
                    }
                });
        userCenter.addMessageItem(ItemUserCard_address);


        //添加测试选框
        AboutPageMessageItem ItemUserCard_test = new AboutPageMessageItem(getActivity())
                .setIcon(getActivity().getDrawable(R.drawable.test))
                .setMainText(getString(R.string.test))
                .setOnItemClickListener(new AboutPageMessageItem.AboutPageOnItemClick() {
                    @Override
                    public void onClick() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder
                                .setTitle(getActivity().getString(R.string.warning))
                                .setMessage(getActivity().getString(R.string.warning_message))
                                .setNeutralButton(getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setPositiveButton(getActivity().getString(R.string.start), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        testDialog = ProgressDialog.show(getActivity(), getText(R.string.test), getString(R.string.testing), true);
                                        new Thread(new check()).start();
                                    }
                                })
                                .create()
                                .show();
                    }
                });
        userCenter.addMessageItem(ItemUserCard_test);

        //添加获取全部选框
        AboutPageMessageItem ItemUserCard_getAll = new AboutPageMessageItem(getActivity())
                .setIcon(getActivity().getDrawable(R.drawable.sync_all))
                .setMainText(getString(R.string.sync_all))
                .setOnItemClickListener(new AboutPageMessageItem.AboutPageOnItemClick() {
                    @Override
                    public void onClick() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder
                                .setTitle(getActivity().getString(R.string.warning))
                                .setMessage(getActivity().getString(R.string.get_all_warning_message))
                                .setNeutralButton(getActivity().getString(R.string.cancel), null)
                                .setPositiveButton(getActivity().getString(R.string.start), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        testDialog = ProgressDialog.show(getActivity(), getText(R.string.sync_all), getString(R.string.syncing), true);
                                        new Thread(new sync()).start();
                                    }
                                })
                                .create()
                                .show();
                    }
                });
        userCenter.addMessageItem(ItemUserCard_getAll);
    }

    //设置中心选框
    private void setSelectionCenter() {

        //添加设置选框
        AboutPageMessageItem ItemSelectionCenter_settings = new AboutPageMessageItem(getActivity())
                .setIcon(getActivity().getDrawable(R.drawable.settings))
                .setMainText(getString(R.string.settings))
                .setOnItemClickListener(new AboutPageMessageItem.AboutPageOnItemClick() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(getActivity(), ActivitySettings.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
        selectionCenter.addMessageItem(ItemSelectionCenter_settings);

        //添加关于选框
        AboutPageMessageItem ItemSelectionCenter_about = new AboutPageMessageItem(getActivity())
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

    //检查线程
    class check implements Runnable {

        @Override
        public void run() {
            String address = sharedPreferences.getString("address", "");
            String username = sharedPreferences.getString("username", "");
            String password = sharedPreferences.getString("password", "");
            isUseful = functions.testURP(address, username, password);
            messageHandler.sendMessage(messageHandler.obtainMessage());
        }
    }

    //获取线程
    class sync implements Runnable {

        @Override
        public void run() {
            String address = sharedPreferences.getString("address", "");
            String username = sharedPreferences.getString("username", "");
            String password = sharedPreferences.getString("password", "");
            ClassIsGot=functions.setClassTableSQL(getActivity(),address,username,password);
            ExamIsGot=functions.setExamInfo(getActivity(),address,username,password);
            GradeIsGot=functions.setGrades(getActivity(),address,username,password);

            messageHandler_getAll.sendMessage(messageHandler_getAll.obtainMessage());

        }
    }



    //异步消息同步
    @SuppressLint("HandlerLeak")
    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            testDialog.dismiss();
            if (isUseful) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getString(R.string.config_useful))
                        .setPositiveButton(getActivity().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create().show();
            } else {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getString(R.string.config_useless))
                        .setPositiveButton(getActivity().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create().show();
            }

            }

    };

    //异步消息同步
    @SuppressLint("HandlerLeak")
    private Handler messageHandler_getAll = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            testDialog.dismiss();
            String result = "";
            if (ClassIsGot && ExamIsGot && GradeIsGot)
                result = getActivity().getString(R.string.all_successful);
            if (!ClassIsGot)
                result+=getActivity().getString(R.string.course_got_error);
            if (!ExamIsGot)
                result+=getActivity().getString(R.string.exam_got_error);
            if (!GradeIsGot)
                result+=getActivity().getString(R.string.grade_got_error);
            new AlertDialog.Builder(getActivity())
                    .setTitle(getActivity().getString(R.string.alert))
                    .setMessage(result)
                    .setPositiveButton(getActivity().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create().show();
        }
    };


}
