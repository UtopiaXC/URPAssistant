package com.utopiaxc.urpassistant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.utopiaxc.urpassistant.R;
import com.zhuangfei.timetable.core.OnSubjectItemClickListener;
import com.zhuangfei.timetable.core.SubjectBean;
import com.zhuangfei.timetable.core.TimetableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentTimeTableChart extends Fragment {

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable_chart, container, false);

        return view;
    }

    //Fragment入口
    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TimetableView mTimetableView =  Objects.requireNonNull(getActivity()).findViewById(R.id.id_timetableView);
        List<Integer> list = new ArrayList<>();
        list.add(1);
        SubjectBean subjectBean = new SubjectBean("1", "203", "a", list,1,2,1,1);
        List<SubjectBean> subjectBeans= new ArrayList<>();
        subjectBeans.add(subjectBean);
        mTimetableView.setDataSource(subjectBeans)
                .setCurTerm("大三上学期")
                .setCurWeek(1)
                .setOnSubjectItemClickListener(new OnSubjectItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<SubjectBean> subjectList) {
                        Toast.makeText(getActivity(),"a",Toast.LENGTH_LONG);
                    }
                })
                .showTimetableView();

    }
}
