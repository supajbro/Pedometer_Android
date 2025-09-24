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
import android.util.Log
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
import com.supajbro.pedometer.ui.theme.PedometerPager
import com.supajbro.pedometer.ui.theme.PedometerTheme
import java.text.SimpleDateFormat
import java.util.*
import com.supajbro.pedometer.ui.theme.PedometerScreen
import org.json.JSONObject

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
        val goal = prefs.getInt("daily_goal", 10000)
        Log.i("TAG", "goal: " + goal)

        setContent {
            PedometerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PedometerPager(steps = steps, goal = goal, stepsPerDay = getWeeklyStepsList())
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
            val currentWeek = getWeekOfYear()
            val savedWeek = prefs.getInt("weekOfYear", -1)
            val dayOfWeek = getDayOfWeek()

            var weeklySteps = prefs.getString("weeklySteps", null)?.let {
                JSONObject(it)
            } ?: JSONObject()

            val savedBaseline = prefs.getFloat("baseline", -1f)

            if (savedWeek != currentWeek) {
                // New week → reset weekly steps
                weeklySteps = JSONObject()
                prefs.edit().putInt("weekOfYear", currentWeek).apply()
            }

            if (savedBaseline < 0 || prefs.getString("date", null) != today) {
                // New day → reset baseline
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

            // Save steps for today in weeklySteps
            weeklySteps.put(dayOfWeek, currentSteps)
            prefs.edit()
                .putString("weeklySteps", weeklySteps.toString())
                .putInt("todaySteps", currentSteps)
                .apply()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getDayOfWeek(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date()) // returns "Monday", "Tuesday", etc.
    }

    fun getWeekOfYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.WEEK_OF_YEAR)
    }

    fun getWeeklySteps(): Map<String, Int> {
        val json = prefs.getString("weeklySteps", null) ?: return emptyMap()
        val map = mutableMapOf<String, Int>()
        val obj = JSONObject(json)
        obj.keys().forEach { key ->
            map[key] = obj.getInt(key)
        }
        return map
    }

    fun getWeeklyStepsList(): List<Int> {
        val weeklySteps = getWeeklySteps() // map of "Monday" to steps
        val daysOfWeekFull = listOf(
            "Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday"
        )

        return daysOfWeekFull.map { day ->
            weeklySteps[day] ?: 0 // default to 0 if no steps for that day
        }
    }
}