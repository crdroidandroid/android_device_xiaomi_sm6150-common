package org.lineageos.settings.device.doze;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;

public class DozeService extends Service {

    private AodSensor mAodSensor;

    @Override
    public void onCreate() {
        super.onCreate();

        mAodSensor = new AodSensor(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(mScreenStateReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = getSystemService(PowerManager.class);

        if (DozeUtils.isAutoBrightnessEnabled(this)
                && !powerManager.isInteractive()) {
            mAodSensor.enable();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mAodSensor.disable();
        unregisterReceiver(mScreenStateReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onDisplayOn() {
        mAodSensor.disable();
    }

    private void onDisplayOff() {
        if (DozeUtils.isAutoBrightnessEnabled(this)) {
            mAodSensor.disable();
            mAodSensor.enable();
        }
    }

    private final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                onDisplayOn();
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                onDisplayOff();
            }
        }
    };
}
