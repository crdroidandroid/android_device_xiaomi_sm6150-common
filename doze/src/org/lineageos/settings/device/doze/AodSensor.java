package org.lineageos.settings.device.doze;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AodSensor implements SensorEventListener {

    private final SensorManager mSensorManager;
    private final Sensor mSensor;

    public AodSensor(Context context) {
        mSensorManager = context.getSystemService(SensorManager.class);
        mSensor = findSensor(mSensorManager, "xiaomi.sensor.aod");
    }

    private static Sensor findSensor(SensorManager sensorManager, String type) {
        for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            if (type.equals(sensor.getStringType()) && sensor.isWakeUpSensor()) {
                return sensor;
            }
        }

        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.values[0] == 3 || event.values[0] == 5) {
            DozeUtils.setDozeMode(DozeUtils.DOZE_MODE_LBM);
        } else if (event.values[0] == 4) {
            DozeUtils.setDozeMode(DozeUtils.DOZE_MODE_HBM);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private boolean mEnabled;

    public void enable() {
        if (mEnabled || mSensor == null) {
            return;
        }

        mSensorManager.registerListener(
                this,
                mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        mEnabled = true;
    }

    public void disable() {
        if (!mEnabled) {
            return;
        }

        mSensorManager.unregisterListener(this);
        mEnabled = false;
    }
}
