/*
 * Copyright (C) 2020 The LineageOS Project
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

package org.lineageos.settings.fps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.UserHandle;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.utils.FileUtils;
import org.lineageos.settings.utils.RefreshRateUtils;

public final class FPSUtils {

    private static final String FPS_CONTROL = "fps_control";
    private static final String FPS_SERVICE = "fps_service";

    protected static final int STATE_120 = 0;
    protected static final int STATE_90 = 1;
    protected static final int STATE_60 = 2;
    protected static final int STATE_30 = 3;

    private static final int FPS_STATE_120 = 4;
    private static final int FPS_STATE_90 = 3;
    private static final int FPS_STATE_60 = 2;
    private static final int FPS_STATE_30 = 1;

    private static final String FPS_120 = "fps.120=";
    private static final String FPS_90 = "fps.90=";
    private static final String FPS_60 = "fps.60=";
    private static final String FPS_30 = "fps.30=";

    private SharedPreferences mSharedPrefs;

    protected FPSUtils(Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void initialize(Context context) {
        if (isServiceEnabled(context))
            startService(context);
        else
            setDefaultFPSProfile();
    }

    protected static void startService(Context context) {
        context.startServiceAsUser(new Intent(context, FPSService.class),
                UserHandle.CURRENT);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(FPS_SERVICE, "true").apply();
    }

    protected static void stopService(Context context) {
        context.stopService(new Intent(context, FPSService.class));
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(FPS_SERVICE, "false").apply();
    }

    protected static boolean isServiceEnabled(Context context) {
        return Boolean.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(FPS_SERVICE, "false"));
    }

    private void writeValue(String profiles) {
        mSharedPrefs.edit().putString(FPS_CONTROL, profiles).apply();
    }

    private String getValue() {
        String value = mSharedPrefs.getString(FPS_CONTROL, null);

        if (value == null || value.isEmpty()) {
            value = FPS_120 + ":" + FPS_90 + ":" + FPS_60 + ":" + FPS_30;
            writeValue(value);
        }
        return value;
    }

    protected void writePackage(String packageName, int mode) {
        String value = getValue();
        value = value.replace(packageName + ",", "");
        String[] modes = value.split(":");
        String finalString;

        switch (mode) {
            case STATE_120:
                modes[0] = modes[0] + packageName + ",";
                break;
            case STATE_90:
                modes[1] = modes[1] + packageName + ",";
                break;
            case STATE_60:
                modes[2] = modes[2] + packageName + ",";
                break;
            case STATE_30:
                modes[3] = modes[3] + packageName + ",";
                break;
        }

        finalString = modes[0] + ":" + modes[1] + ":" + modes[2] + ":" + modes[3];

        writeValue(finalString);
    }

    protected int getStateForPackage(String packageName) {
        String value = getValue();
        String[] modes = value.split(":");
        int state = STATE_120;
        if (modes[0].contains(packageName + ",")) {
            state = STATE_120;
        } else if (modes[1].contains(packageName + ",")) {
            state = STATE_90;
        } else if (modes[2].contains(packageName + ",")) {
            state = STATE_60;
        } else if (modes[3].contains(packageName + ",")) {
            state = STATE_30;
        }

        return state;
    }

    protected static void setDefaultFPSProfile() {
        RefreshRateUtils.setFPS(FPS_STATE_120); 
    }

    protected void setFPSProfile(String packageName) {
        String value = getValue();
        String modes[];
        int state = FPS_STATE_120;

        if (value != null) {
            modes = value.split(":");

            if (modes[0].contains(packageName + ",")) {
                state = FPS_STATE_120;
            } else if (modes[1].contains(packageName + ",")) {
                state = FPS_STATE_90;
            } else if (modes[2].contains(packageName + ",")) {
                state = FPS_STATE_60;
            } else if (modes[3].contains(packageName + ",")) {
                state = FPS_STATE_30;
            }
        }
        RefreshRateUtils.setFPS(state); 
    }
}
