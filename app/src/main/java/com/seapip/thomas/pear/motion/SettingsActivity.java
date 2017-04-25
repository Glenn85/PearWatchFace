package com.seapip.thomas.pear.motion;

import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.GridViewPager;
import android.util.DisplayMetrics;

import com.seapip.thomas.pear.R;
import com.seapip.thomas.pear.settings.SettingsAdapter;
import com.seapip.thomas.pear.settings.SettingsOverlay;
import com.seapip.thomas.pear.settings.SettingsPage;
import com.seapip.thomas.pear.settings.SettingsRow;

import java.util.ArrayList;

public class SettingsActivity extends com.seapip.thomas.pear.settings.SettingsActivity {
    private SettingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext().getApplicationContext());
        adapter = new SettingsAdapter(getFragmentManager()) {
            @Override
            public ArrayList<SettingsRow> initPages() {
                ArrayList<SettingsRow> pages = new ArrayList<>();

                DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;
                int inset = (WatchFaceService.ROUND ? (width - (int) Math.sqrt(width * width / 2)) / 2 : WatchFaceService.MODULE_SPACING) + 20;
                int screenInset = 20;
                Rect screenBounds = new Rect(screenInset, screenInset, width - screenInset, height - screenInset);
                Rect bounds = new Rect(inset, inset, width - inset, height - inset);

                ArrayList<SettingsOverlay> dateModules = new ArrayList<>();
                int date = preferences.getInt("settings_motion_date", 0);
                String dateTitle;
                switch (date) {
                    default:
                    case 0:
                        dateTitle = "Off";
                        break;
                    case 1:
                        dateTitle = "Day of week";
                        break;
                    case 2:
                        dateTitle = "Day of month";
                        break;
                    case 3:
                        dateTitle = "Day";
                        break;
                }
                final SettingsOverlay dateModuleOverlay = new SettingsOverlay(new Rect(
                        bounds.left + WatchFaceService.MODULE_SPACING * 2,
                        bounds.top + bounds.height() / 3 - WatchFaceService.MODULE_SPACING / 2 * 3,
                        bounds.right,
                        bounds.bottom - (bounds.height() - WatchFaceService.MODULE_SPACING * 2) / 3 - 3 * WatchFaceService.MODULE_SPACING),
                        bounds,
                        dateTitle,
                        Paint.Align.RIGHT);
                dateModuleOverlay.setRunnable(new Runnable() {
                    @Override
                    public void run() {
                        int date = preferences.getInt("settings_motion_date", 0);
                        date++;
                        date = date > 3 ? 0 : date;
                        preferences.edit().putInt("settings_motion_date", date).apply();
                        switch (date) {
                            default:
                            case 0:
                                dateModuleOverlay.setTitle("Off");
                                break;
                            case 1:
                                dateModuleOverlay.setTitle("Day of week");
                                break;
                            case 2:
                                dateModuleOverlay.setTitle("Day of month");
                                break;
                            case 3:
                                dateModuleOverlay.setTitle("Day");
                                break;
                        }
                        setSettingsMode(true);
                    }
                });
                dateModuleOverlay.setActive(true);
                dateModules.add(dateModuleOverlay);

                ArrayList<SettingsOverlay> backgroundModules = new ArrayList<>();
                int scene = preferences.getInt("settings_motion_scene", 0);
                String sceneTitle;
                switch (scene) {
                    default:
                    case 0:
                        sceneTitle = "Jellyfish";
                        break;
                    case 1:
                        sceneTitle = "Flowers";
                        break;
                    case 2:
                        sceneTitle = "Cities";
                        break;
                }
                final SettingsOverlay backgroundModuleOverlay = new SettingsOverlay(screenBounds,
                        screenBounds,
                        sceneTitle,
                        Paint.Align.CENTER);
                Runnable sceneRunnable = new Runnable() {
                    @Override
                    public void run() {
                        int scene = preferences.getInt("settings_motion_scene", 0);
                        scene++;
                        scene = scene > 2 ? 0 : scene;
                        preferences.edit().putInt("settings_motion_scene", scene).apply();
                        switch (scene) {
                            case 0:
                                backgroundModuleOverlay.setTitle("Jellyfish");
                                break;
                            case 1:
                                backgroundModuleOverlay.setTitle("Flowers");
                                break;
                            case 2:
                                backgroundModuleOverlay.setTitle("Cities");
                                break;
                        }
                        setSettingsMode(true);
                    }
                };
                backgroundModuleOverlay.setRunnable(sceneRunnable);
                backgroundModuleOverlay.setRound(WatchFaceService.ROUND);
                backgroundModuleOverlay.setInsetTitle(true);
                backgroundModuleOverlay.setActive(true);
                backgroundModules.add(backgroundModuleOverlay);

                SettingsRow row = new SettingsRow();
                row.addPages(new SettingsPage(dateModules));
                row.addPages(new SettingsPage(backgroundModules));
                pages.add(row);

                return pages;
            }
        };
        ((GridViewPager) findViewById(R.id.pager)).setAdapter(adapter);
        setSettingsMode(true);
    }

    @Override
    public void setSettingsMode(boolean mode) {
        WatchFaceService.SETTINGS_MODE = mode ? 3 : 1;
    }

    @Override
    public SettingsAdapter getAdapter() {
        return adapter;
    }
}