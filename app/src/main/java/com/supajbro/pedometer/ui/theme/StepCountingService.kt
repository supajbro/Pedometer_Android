import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.compose.material.icons.R
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepCountingService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var stepSensor: Sensor
    private var baselineSteps: Float = 0f
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (stepSensor == null) {
            Log.e("StepService", "No step counter sensor found!")
        } else {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        //stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)

        startForeground(
            1,
            NotificationHelper.createNotification(this, "Pedometer running")
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        val totalSinceReboot = event.values[0]
        val today = getTodayDate() // format: yyyy-MM-dd

        val savedDate = prefs.getString("date", null)
        val savedBaseline = prefs.getFloat("baseline", -1f)

        if (savedDate != today || savedBaseline < 0) {
            prefs.edit()
                .putString("date", today)
                .putFloat("baseline", totalSinceReboot)
                .apply()
            baselineSteps = totalSinceReboot
        } else {
            baselineSteps = savedBaseline
        }

        val currentSteps = (totalSinceReboot - baselineSteps).toInt()
        prefs.edit().putInt("todaySteps", currentSteps).apply()

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    object NotificationHelper {
        fun createNotification(context: Context, content: String): android.app.Notification {
            val channelId = "pedometer_channel"

            // Create channel on Android 8+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Pedometer Service",
                    android.app.NotificationManager.IMPORTANCE_LOW
                )
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.createNotificationChannel(channel)
            }

            return NotificationCompat.Builder(context, channelId)
                .setContentTitle("Pedometer")
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // works on all versions
                .setOngoing(true) // keeps notification persistent
                .build()
        }
    }


}
