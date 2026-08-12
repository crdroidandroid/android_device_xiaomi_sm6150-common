package org.lineageos.settings.device.doze;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;

import androidx.fragment.app.FragmentActivity;

public class DozeSettingsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View content = findViewById(android.R.id.content);

        content.setOnApplyWindowInsetsListener((v, insets) -> {
            int top = insets.getInsets(
                    WindowInsets.Type.statusBars()).top;

            int bottom = insets.getInsets(
                    WindowInsets.Type.navigationBars()).bottom;

            v.setPadding(
                    v.getPaddingLeft(),
                    top,
                    v.getPaddingRight(),
                    bottom);

            return insets;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new DozeSettingsFragment())
                    .commit();
        }
    }
}
