/**
 *
 * Copyright 2011 AT Computing BV
 *
 * This file is part of Examiner.
 *
 * Examiner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Examiner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Examiner.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.atcomputing.examtrainer.fragments;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.adapters.ExamSelectAdapter;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;

/**
 * @author martijn brekhof
 *
 */

public class ExamSelectFragment extends AbstractFragment {

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.examselectfragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupListView();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public String getTitle() {
        Activity activity = getActivity();
        ApplicationInfo info;
        PackageManager pm = activity.getPackageManager();
        try {
            info = pm.getApplicationInfo("nl.atcomputing.examtrainer", 0);
            return pm.getApplicationLabel(info).toString();
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.selectexam_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.selectexam_menu_about:
                AboutDialogFragment fragment = new AboutDialogFragment();
                fragment.show(getFragmentManager(), null);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void setupListView() {
        final Activity activity = getActivity();
        ListView selectExam = (ListView) activity.findViewById(R.id.select_exam_list);
        TextView noExamsAvailable = (TextView) activity.findViewById(R.id.selectexam_no_exams_available);
        TextView clickOnManageExams = (TextView) activity.findViewById(R.id.selectexam_click_on_manage_exams);

        ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
        examTrainerDbHelper.open();
        Cursor cursor = examTrainerDbHelper.getInstalledAndInstallingExams();
        cursor.moveToFirst();
        examTrainerDbHelper.close();

        ExamSelectAdapter adap = new ExamSelectAdapter(activity, R.layout.examselect_entry, cursor);
        selectExam.setAdapter(adap);

        selectExam.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                abstractFragmentListener.onItemClickListener(id);
            }
        });

        if(cursor.getCount() > 0) {
            //Remove exams not available text when there are exams installed
            noExamsAvailable.setVisibility(View.GONE);
            clickOnManageExams.setVisibility(View.GONE);
        } else {

            noExamsAvailable.setVisibility(View.VISIBLE);
            clickOnManageExams.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateView() {
        setupListView();
    }
}
