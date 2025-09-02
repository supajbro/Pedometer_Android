package com.supajbro.pedometer

import StepCountingService
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.supajbro.pedometer.ui.theme.PedometerTheme
import java.text.SimpleDateFormat
import java.util.*
import com.supajbro.pedometer.ui.theme.PedometerScreen

class MainActivity : ComponentActivity(), SensorEventListener
{
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private val stepCountState = mutableStateOf(0)
    private val prefs by lazy{
        getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)
    }
    private var baselineSteps: Float = -1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Request runtime permission (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 1)
            }
        }

        val intent = Intent(this, StepCountingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        val prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE)
        val steps by stepCountState
        val goal = prefs.getInt("dailyGoal", 10000)

        setContent {
            PedometerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PedometerScreen(steps = steps, goal = goal)
                }
            }
        }
    }

    override  fun onResume(){
        super.onResume()
        stepCounterSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSinceReboot = event.values[0]

            val today = getTodayDate()

            val savedDate = prefs.getString("date", null)
            val savedBaseline = prefs.getFloat("baseline", -1f)

            if (savedDate != today || savedBaseline < 0) {
                // New day or first run → reset baseline
                prefs.edit()
                    .putString("date", today)
                    .putFloat("baseline", totalSinceReboot)
                    .apply()
                baselineSteps = totalSinceReboot
            } else {
                baselineSteps = savedBaseline
            }

            val currentSteps = (totalSinceReboot - baselineSteps).toInt()
            stepCountState.value = currentSteps

            prefs.edit()
                .putInt("todaySteps", currentSteps)
                .apply()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}