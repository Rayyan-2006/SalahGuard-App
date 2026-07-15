package com.salahguard.app.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.salahguard.app.domain.sensor.CompassSensor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AndroidCompassSensor @Inject constructor(
    @ApplicationContext private val context: Context
) : CompassSensor {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    override fun getAzimuth(): Flow<Float> = callbackFlow {
        var gravity: FloatArray? = null
        var geomagnetic: FloatArray? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) gravity = event.values
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) geomagnetic = event.values

                if (gravity != null && geomagnetic != null) {
                    val R = FloatArray(9)
                    val I = FloatArray(9)
                    if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(R, orientation)
                        val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        trySend((azimuth + 360) % 360)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, gsensor, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(listener, msensor, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
