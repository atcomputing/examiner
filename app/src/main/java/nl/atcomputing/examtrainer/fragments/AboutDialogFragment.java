/**
 * 
 * Copyright 2015 AT Computing BV
 *
 * This file is part of Linux Reference Card.
 *
 * Linux Reference Card is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linux Reference Card is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Linux Reference Card.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.atcomputing.examtrainer.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import nl.atcomputing.examtrainer.R;

/**
 * Created by martijn on 8-1-15.
 */
public class AboutDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setInverseBackgroundForced(true);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_about, null);

        String version;
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo("nl.atcomputing.examtrainer", 0);
            version = info.versionName + "-" +info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            version = getString(R.string.unknown);
        }
        TextView tv = (TextView) view.findViewById(R.id.about_version_number);
        tv.setText(version);

        builder.setView(view)
                .setPositiveButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        AboutDialogFragment.this.getDialog().dismiss();
                    }
                });

        return builder.create();
    }
}
