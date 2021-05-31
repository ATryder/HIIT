/*
 * BSD 0-Clause
 * Permission to use, copy, modify, and/or distribute this software
 * for any purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package com.atr.hiit;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import com.atr.hiit.BuildConfig;

/**
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */

public class Help extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.layout_help, null);

        TextView versionView = (TextView)view.findViewById(R.id.version);
        if (versionView != null) {
            try {
                versionView.setText(getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), PackageManager.GET_ACTIVITIES).versionName);
            } catch (Exception e) {
                Log.i("HIIT", "Unable to obtain version name from Package Manager: " + e.getMessage());
                versionView.setText("v" + BuildConfig.VERSION_NAME);
            }
        }

        builder.setView(view).setPositiveButton(getText(R.string.dismiss), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
