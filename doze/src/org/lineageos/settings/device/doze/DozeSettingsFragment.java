package org.lineageos.settings.device.doze;

import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.device.doze.utils.FileUtils;

public class DozeSettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_BRIGHTNESS_LOW = "doze_brightness_low";
    private static final String KEY_BRIGHTNESS_HIGH = "doze_brightness_high";
    private static final String KEY_BRIGHTNESS_ADAPTIVE = "doze_brightness_adaptive";

    private CheckBoxPreference mLow;
    private CheckBoxPreference mHigh;
    private CheckBoxPreference mAdaptive;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.doze_settings, rootKey);

        mLow = findPreference(KEY_BRIGHTNESS_LOW);
        mHigh = findPreference(KEY_BRIGHTNESS_HIGH);
        mAdaptive = findPreference(KEY_BRIGHTNESS_ADAPTIVE);

        if (!FileUtils.isFileWritable(DozeUtils.DOZE_MODE_PATH)) {
            mLow.setEnabled(false);
            mHigh.setEnabled(false);
            mAdaptive.setEnabled(false);
            return;
        }

        mLow.setOnPreferenceChangeListener(this);
        mHigh.setOnPreferenceChangeListener(this);
        mAdaptive.setOnPreferenceChangeListener(this);

        updateBrightnessSelection();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!(Boolean) newValue) {
            return false;
        }

        final String value;

        if (preference == mLow) {
            value = DozeUtils.DOZE_BRIGHTNESS_LBM;
        } else if (preference == mHigh) {
            value = DozeUtils.DOZE_BRIGHTNESS_HBM;
        } else if (preference == mAdaptive) {
            value = DozeUtils.DOZE_BRIGHTNESS_AUTO;
        } else {
            return false;
        }

        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putString(DozeUtils.DOZE_BRIGHTNESS_KEY, value)
                .apply();

        if (!DozeUtils.DOZE_BRIGHTNESS_AUTO.equals(value)) {
            DozeUtils.setDozeMode(value);
        }

        DozeUtils.updateService(requireContext());

        requireView().post(this::updateBrightnessSelection);

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBrightnessSelection();
    }

    private void updateBrightnessSelection() {
        if (mLow == null || mHigh == null || mAdaptive == null) {
            return;
        }

        final String value =
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getString(
                                DozeUtils.DOZE_BRIGHTNESS_KEY,
                                DozeUtils.DOZE_BRIGHTNESS_AUTO);

        mLow.setChecked(DozeUtils.DOZE_BRIGHTNESS_LBM.equals(value));
        mHigh.setChecked(DozeUtils.DOZE_BRIGHTNESS_HBM.equals(value));
        mAdaptive.setChecked(DozeUtils.DOZE_BRIGHTNESS_AUTO.equals(value));
    }
}
