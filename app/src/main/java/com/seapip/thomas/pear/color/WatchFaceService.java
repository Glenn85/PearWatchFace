/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seapip.thomas.pear.color;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.seapip.thomas.pear.module.ClockHandsModule;
import com.seapip.thomas.pear.module.ColorTicksModule;
import com.seapip.thomas.pear.module.ComplicationModule;
import com.seapip.thomas.pear.module.Module;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class WatchFaceService extends CanvasWatchFaceService {

    public static final int[][] COMPLICATION_SUPPORTED_TYPES = {
            {ComplicationData.TYPE_RANGED_VALUE, ComplicationData.TYPE_SHORT_TEXT, ComplicationData.TYPE_SMALL_IMAGE, ComplicationData.TYPE_ICON},
            {ComplicationData.TYPE_RANGED_VALUE, ComplicationData.TYPE_SHORT_TEXT, ComplicationData.TYPE_SMALL_IMAGE, ComplicationData.TYPE_ICON},
            {ComplicationData.TYPE_RANGED_VALUE, ComplicationData.TYPE_SHORT_TEXT, ComplicationData.TYPE_SMALL_IMAGE, ComplicationData.TYPE_ICON},
            {ComplicationData.TYPE_RANGED_VALUE, ComplicationData.TYPE_SHORT_TEXT, ComplicationData.TYPE_SMALL_IMAGE, ComplicationData.TYPE_ICON}
    };
    public static final long INTERACTIVE_UPDATE_RATE_MS = 20;
    private static final int TOP_COMPLICATION = 0;
    private static final int LEFT_COMPLICATION = 1;
    private static final int RIGHT_COMPLICATION = 2;
    private static final int BOTTOM_COMPLICATION = 3;
    public static final int[] COMPLICATION_IDS = {
            TOP_COMPLICATION,
            LEFT_COMPLICATION,
            RIGHT_COMPLICATION,
            BOTTOM_COMPLICATION
    };
    private static final int MSG_UPDATE_TIME = 0;

    public static int SETTINGS_MODE = 0;

    public static boolean ROUND = false;

    private SharedPreferences mPrefs;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<WatchFaceService.Engine> mWeakReference;

        public EngineHandler(WatchFaceService.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            WatchFaceService.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private final Handler mUpdateTimeHandler = new EngineHandler(this);

        private Calendar mCalendar;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        private boolean mRegisteredTimeZoneReceiver = false;

        /* Display */
        private int mWidth;
        private int mHeight;
        private boolean mIsRound;
        private boolean mAmbient;

        /*Modules */
        private ArrayList<Module> mModules;
        private ComplicationModule mTopComplicationModule;
        private ComplicationModule mLeftComplicationModule;
        private ComplicationModule mRightComplicationModule;
        private ComplicationModule mBottomComplicationModule;
        private ColorTicksModule mColorTicksModule;
        private ClockHandsModule mClockHandsModule;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFaceService.this)
                    .setStatusBarGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                    .setAcceptsTapEvents(true)
                    .build());

            mCalendar = Calendar.getInstance();
            mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            setActiveComplications(COMPLICATION_IDS);

            mTopComplicationModule = new ComplicationModule(getApplicationContext());
            mLeftComplicationModule = new ComplicationModule(getApplicationContext());
            mRightComplicationModule = new ComplicationModule(getApplicationContext());
            mBottomComplicationModule = new ComplicationModule(getApplicationContext());
            mColorTicksModule = new ColorTicksModule();
            mClockHandsModule = new ClockHandsModule(mCalendar);

            mModules = new ArrayList<>();
            mModules.add(mTopComplicationModule);
            mModules.add(mLeftComplicationModule);
            mModules.add(mRightComplicationModule);
            mModules.add(mBottomComplicationModule);
            mModules.add(mColorTicksModule);
            mModules.add(mClockHandsModule);

            int color = mPrefs.getInt("settings_color_color_value", Color.parseColor("#00BCD4"));
            int accentColor = mPrefs.getInt("settings_color_accent_color_value",
                    Color.parseColor("#CDDC39"));
            for (Module module : mModules) {
                module.setColor(color);
            }
            mClockHandsModule.setColor(accentColor);
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            for (Module module : mModules) {
                module.setBurnInProtection(properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false));
                module.setLowBitAmbient(properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false));
            }
        }

        @Override
        public void onComplicationDataUpdate(int complicationId,
                                             ComplicationData complicationData) {
            ((ComplicationModule) mModules.get(complicationId)).setComplicationData(complicationData);
            invalidate();
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mAmbient = inAmbientMode;
            for (Module module : mModules) {
                module.setAmbient(inAmbientMode);
            }
            updateTimer();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mIsRound = insets.isRound();
            ROUND = mIsRound;
            setBounds();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mWidth = width;
            mHeight = height;
            setBounds();
        }

        private void setBounds() {
            int inset = 10;
            if (SETTINGS_MODE == 3) {
                inset += 20;
            }

            Rect bounds = new Rect(inset, inset, mWidth - inset, mHeight - inset);

            int offset = (int) (bounds.height() * 0.18f);
            int size = (int) (bounds.width() * 0.20f);
            mTopComplicationModule.setBounds(new Rect(bounds.centerX() - size / 2,
                    bounds.top + offset,
                    bounds.centerX() + size / 2,
                    bounds.top + offset + size));
            mLeftComplicationModule.setBounds(new Rect(bounds.left + offset,
                    bounds.centerY() - size / 2,
                    bounds.left + offset + size,
                    bounds.centerY() + size / 2));
            mRightComplicationModule.setBounds(new Rect(bounds.right - offset - size,
                    bounds.centerY() - size / 2,
                    bounds.right - offset,
                    bounds.centerY() + size / 2));
            mBottomComplicationModule.setBounds(new Rect(bounds.centerX() - size / 2,
                    bounds.bottom - offset - size,
                    bounds.centerX() + size / 2,
                    bounds.bottom - offset));
            mColorTicksModule.setBounds(bounds);
            mClockHandsModule.setBounds(bounds);
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    for (Module module : mModules) {
                        if (module instanceof ComplicationModule &&
                                ((ComplicationModule) module).contains(x, y)) {
                            PendingIntent intent = ((ComplicationModule) module).getTapAction();
                            if (intent != null) {
                                try {
                                    intent.send();
                                } catch (PendingIntent.CanceledException e) {
                                }
                            }
                        }
                    }
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            switch (SETTINGS_MODE) {
                case 1:
                    setBounds();
                    SETTINGS_MODE = 0;
                    break;
                case 2:
                    mCalendar.set(Calendar.HOUR, 10);
                    mCalendar.set(Calendar.MINUTE, 10);
                    mCalendar.set(Calendar.SECOND, 30);
                    mCalendar.set(Calendar.MILLISECOND, 0);
                    break;
                case 3:
                    setBounds();
                    int color = mPrefs.getInt("settings_color_color_value",
                            Color.parseColor("#00BCD4"));
                    int accentColor = mPrefs.getInt("settings_color_accent_color_value",
                            Color.parseColor("#CDDC39"));
                    for (Module module : mModules) {
                        module.setColor(color);
                    }
                    mClockHandsModule.setColor(accentColor);
                    SETTINGS_MODE = 2;
                    break;
            }

            canvas.drawColor(Color.BLACK);
            for (Module module : mModules) {
                if (module instanceof ComplicationModule) {
                    ((ComplicationModule) module).setCurrentTimeMillis(now);
                }
                module.draw(canvas);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();
                /* Update time zone in case it changed while we weren't visible. */
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            WatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            WatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
