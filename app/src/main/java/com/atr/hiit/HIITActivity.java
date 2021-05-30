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

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

/**
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */

public class HIITActivity extends AppCompatActivity {
    private long activePeriod = 20000;
    private long restPeriod = 10000;
    private long delay = 2000;

    private Fragment frag;

    private SoundPool soundPool;
    private int alarmSound;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hiit);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createSoundPool();
        } else {
            createOldSoundPool();
        }
        //soundMap.put(R.raw.beep, soundPool.load(this, R.raw.beep, 1));
        alarmSound = soundPool.load(this, R.raw.beep, 1);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);

        if (savedInstanceState == null) {
            SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
            activePeriod = prefs.getLong("activePeriod", 20000);
            restPeriod = prefs.getLong("restPeriod", 10000);
            delay = prefs.getLong("delay", 2000);

            frag = Settings.newInstance(activePeriod, restPeriod, delay);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.activitycontent, frag);
            ft.commit();
        } else {
            activePeriod = savedInstanceState.getLong("activePeriod", 20000);
            restPeriod = savedInstanceState.getLong("restPeriod", 10000);
            delay = savedInstanceState.getLong("delay", 2000);

            frag = getSupportFragmentManager().findFragmentById(R.id.activitycontent);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(2)
                .build();
    }

    @SuppressWarnings("deprecation")
    private void createOldSoundPool() {
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
    }

    protected void playAlarm() {
        int streamID = soundPool.play(alarmSound, 1f, 1f, 1, 0, 1f);
    }

    protected void captureSettings() {
        Bundle bundle = ((Settings)frag).save();
        activePeriod = bundle.getLong("activePeriod", activePeriod);
        restPeriod = bundle.getLong("restPeriod", restPeriod);
        delay = bundle.getLong("delay", delay);
    }

    protected void swapFrag() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.frag_in, R.anim.frag_out);

        ft.remove(frag);
        Fragment newFrag = (frag instanceof Settings) ? Timer.newInstance(activePeriod, restPeriod, delay)
                : Settings.newInstance(activePeriod, restPeriod, delay);
        ft.add(R.id.activitycontent, newFrag);
        frag = newFrag;

        ft.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!(frag instanceof Settings))
            return;

        captureSettings();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("activePeriod", activePeriod);
        outState.putLong("restPeriod", restPeriod);
        outState.putLong("delay", delay);
    }

    protected void onDestroy() {
        super.onDestroy();

        if (soundPool != null)
            soundPool.release();

        SharedPreferences.Editor prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
        prefs.putLong("activePeriod", activePeriod);
        prefs.putLong("restPeriod", restPeriod);
        prefs.putLong("delay", delay);
        prefs.apply();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (frag instanceof Timer) {
                swapFrag();
            } else
                finish();
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
}
