package org.lineageos.settings.device.doze;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.device.doze.utils.FileUtils;

public final class DozeUtils {

    protected static final String DOZE_BRIGHTNESS_KEY = "doze_brightness";

    protected static final String DOZE_MODE_PATH =
            "/sys/devices/platform/soc/soc:qcom,dsi-display/doze_mode";

    protected static final String DOZE_MODE_LBM = "0";
    protected static final String DOZE_MODE_HBM = "1";

    protected static final String DOZE_BRIGHTNESS_LBM = "0";
    protected static final String DOZE_BRIGHTNESS_HBM = "1";
    protected static final String DOZE_BRIGHTNESS_AUTO = "2";

    private DozeUtils() {}

    protected static boolean setDozeMode(String value) {
        return FileUtils.writeLine(DOZE_MODE_PATH, value);
    }

    protected static boolean isAutoBrightnessEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(DOZE_BRIGHTNESS_KEY, DOZE_BRIGHTNESS_AUTO)
                .equals(DOZE_BRIGHTNESS_AUTO);
    }

    protected static void updateService(Context context) {
        Intent intent = new Intent(context, DozeService.class);

        if (isAutoBrightnessEnabled(context)) {
            context.startServiceAsUser(intent, UserHandle.CURRENT);
        } else {
            context.stopServiceAsUser(intent, UserHandle.CURRENT);
        }
    }

    protected static void restoreBrightness(Context context) {
        String value = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(DOZE_BRIGHTNESS_KEY, DOZE_BRIGHTNESS_AUTO);

        if (!DOZE_BRIGHTNESS_AUTO.equals(value)) {
            setDozeMode(value);
        }
    }
}
