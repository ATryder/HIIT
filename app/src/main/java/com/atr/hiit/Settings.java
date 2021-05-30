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

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */

public class Settings extends Fragment {
    private long activePeriod;
    private long restPeriod;
    private long delay;

    private HIITActivity ctx;

    private boolean starting = false;

    public static Settings newInstance(long activePeriod, long restPeriod, long delay) {
        Bundle bundle = new Bundle();
        bundle.putLong("settings.activePeriod", activePeriod);
        bundle.putLong("settings.restPeriod", restPeriod);
        bundle.putLong("settings.delay", delay);

        Settings settings = new Settings();
        settings.setArguments(bundle);

        return settings;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ctx = (HIITActivity)context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            activePeriod = getArguments().getLong("settings.activePeriod", 20000);
            restPeriod = getArguments().getLong("settings.restPeriod", 10000);
            delay = getArguments().getLong("settings.delay", 2000);
        } else {
            activePeriod = savedInstanceState.getLong("settings.activePeriod", 20000);
            restPeriod = savedInstanceState.getLong("settings.restPeriod", 10000);
            delay = savedInstanceState.getLong("settings.delay", 2000);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((EditText)view.findViewById(R.id.activePeriod)).setText(Long.toString(activePeriod / 1000));
        ((EditText)view.findViewById(R.id.restPeriod)).setText(Long.toString(restPeriod / 1000));
        ((EditText)view.findViewById(R.id.delay)).setText(Long.toString(delay / 1000));

        view.findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });

        view.findViewById(R.id.helpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                help();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (starting)
            return;

        setPeriods();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("settings.activePeriod", activePeriod);
        outState.putLong("settings.restPeriod", restPeriod);
        outState.putLong("settings.delay", delay);
    }

    private void setPeriods() {
        if (((EditText)getView().findViewById(R.id.activePeriod)).getText().length() == 0) {
            activePeriod = 5000;
        } else
            activePeriod = Long.parseLong(((EditText)getView().findViewById(R.id.activePeriod)).getText().toString()) * 1000;

        if (((EditText)getView().findViewById(R.id.restPeriod)).getText().length() == 0) {
            restPeriod = 0;
        } else
            restPeriod = Long.parseLong(((EditText)getView().findViewById(R.id.restPeriod)).getText().toString()) * 1000;

        if (((EditText)getView().findViewById(R.id.delay)).getText().length() == 0) {
            delay = 0;
        } else
            delay = Long.parseLong(((EditText)getView().findViewById(R.id.delay)).getText().toString()) * 1000;

        if (activePeriod < 0)
            activePeriod = -activePeriod;
        if (restPeriod < 0)
            restPeriod = 0;
        if (delay < 0)
            delay = 0;
    }

    protected Bundle save() {
        setPeriods();

        Bundle bundle = new Bundle();
        bundle.putLong("activePeriod", activePeriod);
        bundle.putLong("restPeriod", restPeriod);
        bundle.putLong("delay", delay);

        return bundle;
    }

    private void start() {
        if (activePeriod <= 0)
            return;

        starting = true;

        getView().findViewById(R.id.activePeriod).setEnabled(false);
        getView().findViewById(R.id.restPeriod).setEnabled(false);
        getView().findViewById(R.id.delay).setEnabled(false);
        getView().findViewById(R.id.startButton).setEnabled(false);

        ctx.captureSettings();
        ctx.swapFrag();
    }

    private void help() {
        new Help().show(ctx.getSupportFragmentManager(), "HelpDialog");
    }
}
