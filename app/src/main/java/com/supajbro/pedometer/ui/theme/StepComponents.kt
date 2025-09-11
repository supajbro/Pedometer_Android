package com.supajbro.pedometer.ui.theme

import android.content.Context
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight


@Composable
fun PedometerScreen(steps: Int, goal: Int) {
    var useMiles by remember { mutableStateOf(false) }

// Animated multi-color gradient
    val infiniteTransition = rememberInfiniteTransition()

    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF1A3A4B),
        targetValue = Color(0xFF3D7589),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF3D7589),
        targetValue = Color(0xFF2B5262),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color3 by infiniteTransition.animateColor(
        initialValue = Color(0xFF2B5262),
        targetValue = Color(0xFF1A3A4B),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color4 by infiniteTransition.animateColor(
        initialValue = Color(0xFF3D7589),
        targetValue = Color(0xFF7CA1B0),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animate gradient positions
    val startX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4000f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val startY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4000f,
        animationSpec = infiniteRepeatable(
            animation = tween(6500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val endX by infiniteTransition.animateFloat(
        initialValue = 4000f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val endY by infiniteTransition.animateFloat(
        initialValue = 4000f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(color1, color2, color3, color4),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY)
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Title()
        Spacer(modifier = Modifier.weight(1f))

        TotalDistance(steps = steps, useMiles)
        Spacer(modifier = Modifier.weight(1f))

        StepCounter(steps = steps, goal = goal)
        Spacer(modifier = Modifier.weight(1f))

        DailyGoalProgress(steps = steps, goal = goal)

        // Button at bottom to toggle km/miles
        Button(
            onClick = { useMiles = !useMiles },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(
                text = if (useMiles) "Show in KM" else "Show in Miles",
                color = Color.White
            )
        }
    }
}

@Composable
fun Title(){
    val infiniteTransition = rememberInfiniteTransition()

    // Animate flickering color between yellow, orange, and red
    val animatedColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFFFA500), // Orange
        targetValue = Color(0xFFFF4500), // Red-Orange
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animate slight scale for flicker effect
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Text(
        text = "Step Buddy",
        fontSize = 50.sp,
        color = animatedColor,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(top = 16.dp)
            .scale(animatedScale)
    )
}

@Composable
fun TotalDistance(steps: Int, useMiles: Boolean) {
    val strideLengthMeters = 0.78f
    val distanceKm = (steps * strideLengthMeters / 1000f)
    val distance = if (useMiles) distanceKm * 0.621371f else distanceKm
    val unitText = if (useMiles) "mi" else "km"
    val distanceText = String.format("%.2f", distance)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Total Distance Today:",
            fontSize = 28.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Color.Cyan,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
        )

        Text(
            text = "$distanceText $unitText",
            fontSize = 32.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Color.Cyan,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
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

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(220.dp) // Circle size
            .padding(16.dp)
    ) {
        // Outer circular progress (goal ring)
        CircularProgressIndicator(
            progress = progress,
            strokeWidth = 12.dp,
            color = Color.Cyan,
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
                text = "$steps",
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
            text = "Daily Goal: $goal steps",
            fontSize =  20.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${(progress* 100).toInt()}% complete",
            fontSize = 18.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Color.Cyan
        )
    }
}
