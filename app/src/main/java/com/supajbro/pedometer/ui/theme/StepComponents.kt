package com.supajbro.pedometer.ui.theme

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush


@Composable
fun PedometerScreen(steps: Int) {
    var useMiles by remember { mutableStateOf(false) }

    // Animated gradient colors
    val infiniteTransition = rememberInfiniteTransition()
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF0F2027),
        targetValue = Color(0xFF2C5364),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF2C5364),
        targetValue = Color(0xFF203A43),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(color1, color2)
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TotalDistance(steps = steps, useMiles)
        Spacer(modifier = Modifier.weight(1f))

        StepCounter(steps = steps)
        Spacer(modifier = Modifier.weight(1f))

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
            color = Color.Cyan,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
        )

        Text(
            text = "$distanceText $unitText",
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
    val scale by animateFloatAsState(
        targetValue = if (steps > previousSteps) 1.2f else 1f,
        label = "scaleAnimation"
    )

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

        Spacer(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "$steps",
            fontSize = 48.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.scale(scale)
        )
    }
}
