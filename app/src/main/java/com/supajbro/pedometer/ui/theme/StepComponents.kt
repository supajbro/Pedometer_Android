package com.supajbro.pedometer.ui.theme

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
//import androidx.compose.runtime.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supajbro.pedometer.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.lerp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import kotlin.math.roundToInt
import android.os.VibrationEffect
import android.os.Vibrator

@Composable
fun PedometerPager(steps: Int, goal: Int){
    // Animated multi-color gradient
    val infiniteTransition = rememberInfiniteTransition()

    val animatedBlue by infiniteTransition.animateColor(
        initialValue = Color.Blue,
        targetValue = Color.Cyan, // change to whatever color you want to cycle to
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000), // 3 seconds fade
            repeatMode = RepeatMode.Reverse // fades back and forth
        )
    )

    val animatedOrange by infiniteTransition.animateColor(
        initialValue = Color(0xFFEF9224),
        targetValue = Color(0xFFF35E2F),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000), // 3 seconds fade
            repeatMode = RepeatMode.Reverse // fades back and forth
        )
    )

    // Background
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        animatedOrange,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        animatedOrange
                    ),
                    start = Offset(0f, 0f), // Top-left corner
                    end = Offset(
                        Float.POSITIVE_INFINITY,
                        Float.POSITIVE_INFINITY
                    ) // Bottom-right corner
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ){

    }

    val context = LocalContext.current
    var targetScreen by remember { mutableStateOf(0) }
    var activeScreen by remember { mutableStateOf(0) }
    var dailyGoal by remember { mutableStateOf(
        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE)
            .getInt("daily_goal", 10000)) }

    // Handle delayed screen switch
    LaunchedEffect(targetScreen) {
        targetScreen?.let { newScreen ->
            activeScreen = -1          // hide current screen
            delay(200)                 // wait for exit animation
            activeScreen = newScreen   // show new screen
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        val dampening = 0.45f // lower = more bounce
        val stiffness = 300f // lower = slower bounce

        // Pedometer Screen
        AnimatedVisibility(
            visible = activeScreen == 0,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = dampening,
                    stiffness = stiffness
                ),
                initialScale = 0f
            ),
            exit = scaleOut(tween(200), targetScale = 0f)
        ) {
            PedometerScreen(steps = steps, goal = dailyGoal)
        }

        // Daily Goal Screen
        AnimatedVisibility(
            visible = activeScreen == 1,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = dampening,
                    stiffness = stiffness
                ),
                initialScale = 0f
            ),
            exit = scaleOut(tween(200), targetScale = 0f)
        ) {
            DailyGoalScreen(oal = dailyGoal, onGoalChange = { newGoal -> dailyGoal = newGoal })
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick =
            {
                targetScreen = 0
                context.vibrate(50)
            }) {
                Text("Home")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick =
            {
                targetScreen = 1
                context.vibrate(50)
            }) {
                Text("Goal Setup")
            }
        }
    }
}

@Composable
fun PedometerScreen(steps: Int, goal: Int) {
    var useMiles by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Title()
        //Spacer(modifier = Modifier.weight(1f))

        StepCounter(steps = steps, goal = goal)
        //Spacer(modifier = Modifier.weight(1f))

        TotalDistance(steps = steps, useMiles, goal = goal)
        //Spacer(modifier = Modifier.weight(.5f))

        //DailyGoalProgress(steps = steps, goal = goal)

        //// Button at bottom to toggle km/miles
        //Button(
        //    onClick = { useMiles = !useMiles },
        //    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
        //    modifier = Modifier.padding(bottom = 32.dp)
        //) {
        //    Text(
        //        text = if (useMiles) "Show in KM" else "Show in Miles",
        //        color = Color.White
        //    )
        //}
    }
}

@Composable
fun Title(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.pacepal), // Use your file name here
            contentDescription = "Title",
            modifier = Modifier
                .size(250.dp) // Set the size you want
                .padding(0.dp)
        )
    }
}

@Composable
fun TotalDistance(steps: Int, useMiles: Boolean, goal: Int) {
    val strideLengthMeters = 0.78f
    val distanceKm = (steps * strideLengthMeters / 1000f)
    val distance = if (useMiles) distanceKm * 0.621371f else distanceKm
    val unitText = if (useMiles) "mi" else "km"

    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    // Animate distance text
    val animatedDistance = remember { Animatable(0f) }
    LaunchedEffect(distance) {
        delay(1000)
        animatedDistance.animateTo(
            targetValue = distance.toFloat(),
            animationSpec = tween(durationMillis = 1000)
        )
    }
    val distanceText = String.format("%.2f", animatedDistance.value)

    // Animate goal steps text
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        delay(1000)
        animatedProgress.animateTo(
            targetValue = progress.toFloat(),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Text(
        //    text = "Total Distance Today:",
        //    fontSize = 12.sp,
        //    fontFamily = FontFamily.SansSerif,
        //    fontWeight = FontWeight.Bold,
        //    color = Color(0xFFEF9224),
        //    textAlign = TextAlign.Center,
        //    modifier = Modifier.padding(bottom = 4.dp) // smaller spacing below
        //)

        Text(
            text = "$distanceText $unitText",
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEF9224),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 0.dp) // no extra top padding
        )

        Text(
            text = "Goal: $goal steps",
            fontSize =  12.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEF9224)
        )

        Text(
            text = "${(animatedProgress.value * 100).toInt()}% to daily goal",
            fontSize = 16.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEF9224)
        )
    }
}

@Composable
fun StepCounter(steps: Int, goal: Int) {
    var previousSteps by remember { mutableStateOf(steps) }
    val scale by animateFloatAsState(
        targetValue = if (steps > previousSteps) 1.2f else 1f,
        label = "scaleAnimation"
    )

    LaunchedEffect(steps) {
        if (steps > previousSteps) previousSteps = steps
    }

    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    // Animate progress circle
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        delay(1000)
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1000) // 1s animation
        )
    }

    // Animate steps text
    val animatedSteps = remember { Animatable(0f) }
    LaunchedEffect(steps) {
        delay(1000)
        animatedSteps.animateTo(
            targetValue = steps.toFloat(),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(220.dp) // Circle size
            .padding(16.dp)
    ) {

        // Grey circle that is always full
        CircularProgressIndicator(
            progress = goal.toFloat(),
            strokeWidth = 12.dp,
            color = Color.Gray,
            modifier = Modifier.fillMaxSize()
        )

        // Outer circular progress (goal ring)
        CircularProgressIndicator(
            progress = animatedProgress.value,
            strokeWidth = 12.dp,
            color = Color(0xFFEF9224),
            modifier = Modifier.fillMaxSize()
        )

        // Inner content (step text)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Steps today:",
                fontSize = 24.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${animatedSteps.value.roundToInt()}",
                fontSize = 40.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(scale)
            )
        }
    }
}

@Composable
fun DailyGoalProgress(steps: Int, goal: Int){
    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Daily Goal:",
            fontSize =  16.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$goal steps",
            fontSize =  20.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyGoalScreen(oal: Int, onGoalChange: (Int) -> Unit){
    val context = LocalContext.current
    var dailyGoal by remember { mutableStateOf(
        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE)
            .getInt("daily_goal", 10000)) }
    var inputText by remember {mutableStateOf("")}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            //Spacer(modifier = Modifier.height(8.dp))
//
            //Text(
            //    text = "Daily Goal:",
            //    fontSize = 20.sp,
            //    fontFamily = FontFamily.SansSerif,
            //    fontWeight = FontWeight.Bold,
            //    color = Color.White
            //)
//
            //Spacer(modifier = Modifier.height(8.dp))
//
            //Text(
            //    text = "$dailyGoal steps",
            //    fontSize = 24.sp,
            //    fontFamily = FontFamily.SansSerif,
            //    fontWeight = FontWeight.Bold,
            //    color = Color.White
            //)
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Enter new daily goal:",
                fontSize = 20.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("e.g. 6000") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.textFieldColors(
                    disabledTextColor = Color.White,
                    disabledPlaceholderColor = Color.Gray,
                    disabledSupportingTextColor = Color.DarkGray
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    inputText.toIntOrNull()?.let { newGoal ->
                        dailyGoal = newGoal
                        val prefs = context.getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                        prefs.edit().putInt("daily_goal", dailyGoal).apply()
                        Log.i("TAG", "updated daily goal: " + prefs.getInt("daily_goal", 10000))
                        onGoalChange(newGoal)
                        inputText = ""
                        context.vibrate(50)
                    }
                }
            ) {
                Text("Set Goal")
            }
        }
    }
}

fun Context.vibrate(duration: Long = 100) {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    } else {
        // Deprecated in API 26
        vibrator.vibrate(duration)
    }
}
