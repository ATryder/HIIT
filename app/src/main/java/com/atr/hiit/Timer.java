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
import android.os.CountDownTimer;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */

public class Timer extends Fragment {
    private long activePeriod;
    private long restPeriod;
    private long delay;

    private HIITActivity ctx;

    private String period = "ACTIVE";
    private int set = 0;
    private long elapsed = 0;
    private boolean started = false;
    private boolean paused = false;

    private CountDownTimer timer;

    private ProgressBar pb;
    private TextView timeDisplay;
    private TextView setDisplay;
    private TextView periodDisplay;

    private boolean delayPlayed = false;

    protected static Timer newInstance(long activePeriod, long restPeriod, long delay) {
        Bundle bundle = new Bundle();
        bundle.putLong("activePeriod", activePeriod);
        bundle.putLong("restPeriod", restPeriod);
        bundle.putLong("delay", delay);

        Timer timer = new Timer();
        timer.setArguments(bundle);

        return timer;
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
            activePeriod = getArguments().getLong("activePeriod", 20000);
            restPeriod = getArguments().getLong("restPeriod", 10000);
            delay = getArguments().getLong("delay", 2000);
            period = getText(R.string.active).toString();
        } else {
            activePeriod = savedInstanceState.getLong("activePeriod", 20000);
            restPeriod = savedInstanceState.getLong("restPeriod", 10000);
            delay = savedInstanceState.getLong("delay", 2000);

            started = savedInstanceState.getBoolean("started", false);
            if (started) {
                paused = true;
                period = savedInstanceState.getString("period", getText(R.string.active).toString());
                set = savedInstanceState.getInt("set", 0);
                elapsed = savedInstanceState.getLong("elapsed", 0);
                if (period.equals(getText(R.string.rest).toString()))
                    delayPlayed = savedInstanceState.getBoolean("delayPlayed", false);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_timer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        pb = (ProgressBar) view.findViewById(R.id.progressBar);
        setDisplay = (TextView)view.findViewById(R.id.setText);
        timeDisplay = (TextView)view.findViewById(R.id.timerDisplay);
        periodDisplay = (TextView)view.findViewById(R.id.periodText);

        view.findViewById(R.id.startTimer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
            }
        });

        view.findViewById(R.id.stopTimer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimer();
            }
        });

        if (!started)
            return;

        setDisplay.setText(Integer.toString(set));
        timeDisplay.setText(Long.toString(elapsed / 1000));
        periodDisplay.setText(period);

        if (period.equals(getText(R.string.active).toString())) {
            pb.setMax((int)(activePeriod));
        } else
            pb.setMax((int)restPeriod);
        pb.setProgress((int)elapsed);
    }

    @Override
    public void onResume() {
        super.onResume();

        ctx.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();

        ctx.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!started)
            return;

        pause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("activePeriod", activePeriod);
        outState.putLong("restPeriod", restPeriod);
        outState.putLong("delay", delay);

        outState.putString("period", period);
        outState.putInt("set", set);
        outState.putLong("elapsed", elapsed);
        outState.putBoolean("started", started);
        outState.putBoolean("delayAlarm", delayPlayed);
    }

    private void startCounting() {
        if (period.equals(getText(R.string.active).toString())) {
            pb.setMax((int)activePeriod);
            timer = new CountDownTimer(activePeriod - elapsed, 42) {
                @Override
                public void onTick(long millisRemaining) {
                    elapsed = activePeriod - millisRemaining;
                    elapsed = elapsed > activePeriod ? activePeriod : elapsed;
                    pb.setProgress((int)elapsed);
                    timeDisplay.setText(Long.toString(elapsed / 1000));
                }

                @Override
                public void onFinish() {
                    elapsed = 0;
                    pb.setProgress(0);
                    timeDisplay.setText("0");
                    if (restPeriod > 0) {
                        period = getText(R.string.rest).toString();
                        periodDisplay.setText(period);
                    } else {
                        set += 1;
                        setDisplay.setText(Integer.toString(set));
                    }

                    ctx.playAlarm();
                    startCounting();
                }
            }.start();

            return;
        }

        pb.setMax((int)restPeriod);
        timer = new CountDownTimer(restPeriod - elapsed, 42) {
            @Override
            public void onTick(long millisRemaining) {
                elapsed = restPeriod - millisRemaining;
                elapsed = elapsed > restPeriod ? restPeriod : elapsed;
                pb.setProgress((int)elapsed);
                timeDisplay.setText(Long.toString(elapsed / 1000));

                if (delay > 0 && delay < restPeriod && millisRemaining <= delay && !delayPlayed) {
                    delayPlayed = true;
                    ctx.playAlarm();
                }
            }

            @Override
            public void onFinish() {
                elapsed = 0;
                set += 1;
                pb.setProgress(0);
                timeDisplay.setText("0");
                setDisplay.setText(Integer.toString(set));
                period = getText(R.string.active).toString();
                periodDisplay.setText(period);

                delayPlayed = false;
                ctx.playAlarm();
                startCounting();
            }
        }.start();
    }

    private void startTimer() {
        if (started) {
            pauseTimer();
            return;
        }

        ((Button)getView().findViewById(R.id.startTimer)).setText(getText(R.string.pause));
        started = true;
        set += 1;
        setDisplay.setText(Integer.toString(set));
        startCounting();
    }

    private void pauseTimer() {
        if (paused) {
            unPause();
        } else
            pause();
    }

    private void unPause() {
        paused = false;
        ((Button)getView().findViewById(R.id.startTimer)).setText(getText(R.string.pause));
        startCounting();
    }

    private void pause() {
        paused = true;
        ((Button)getView().findViewById(R.id.startTimer)).setText(getText(R.string.start));
        if (timer != null)
            timer.cancel();
    }

    private void stopTimer() {
        if (!started) {
            ctx.swapFrag();
            return;
        }

        ((Button)getView().findViewById(R.id.startTimer)).setText(getText(R.string.start));

        period = getText(R.string.active).toString();
        elapsed = 0;
        set = 0;
        started = false;
        paused = false;
        delayPlayed = false;
        if (timer != null)
            timer.cancel();
        timer = null;

        periodDisplay.setText(period);
        setDisplay.setText("0");
        timeDisplay.setText("0");
        pb.setMax((int)activePeriod);
        pb.setProgress(0);
    }
}
