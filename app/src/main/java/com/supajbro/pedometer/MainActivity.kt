package com.supajbro.pedometer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.supajbro.pedometer.ui.theme.PedometerTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

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

        setContent {
            PedometerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PedometerScreen(stepCountState.value)
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
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}

@Composable
fun PedometerScreen(steps: Int) {
    // You can adjust spacing and alignment here
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        //verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TotalDistance(steps = steps)
        Spacer(modifier = Modifier.weight(1f))

        StepCounter(steps = steps)
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun TotalDistance(steps: Int) {
    // Calculate distance in km
    val strideLengthMeters = 0.78f
    val distanceKm = (steps * strideLengthMeters / 1000f)
    val distanceText = String.format("%.2f",distanceKm)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Total KMs today:",
            fontSize = 28.sp,
            color = Color.Cyan,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
        )

        Text(
            text = "$distanceText KM",
            fontSize = 32.sp,
            color = Color.Cyan,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
        )
    }
}

@Composable
fun StepCounter(steps: Int) {
    var previousSteps by remember { mutableStateOf(steps) }
    val targetSteps = if (steps > previousSteps) steps else previousSteps
    val animatedSteps by animateIntAsState(targetValue = targetSteps, label = "stepAnimation")
    val scale by animateFloatAsState(
        targetValue = if (steps > previousSteps) 1.2f else 1f,
        label = "scaleAnimation"
    )

    // Update previousSteps after animation logic
    LaunchedEffect(steps) {
        if (steps > previousSteps) previousSteps = steps
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Steps today:",
            fontSize = 32.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        // Add space between label and count
        Spacer(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "$animatedSteps",
            fontSize = 48.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .scale(scale)
                .shadow(10.dp, ambientColor = Color.White, spotColor = Color.White)
        )
    }
}