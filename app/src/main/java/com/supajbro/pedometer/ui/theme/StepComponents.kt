package com.supajbro.pedometer.ui.theme

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PedometerPager(steps: Int, goal: Int){
    // Page count as a State (source of truth)
    var pageCount by remember { mutableStateOf(2) }

    // Pager state with pageCount
    val pagerState = rememberPagerState(pageCount = { pageCount })

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

    // Background
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        animatedBlue,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        animatedBlue
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

    var activeScreen by remember { mutableStateOf(0) }
    var dailyGoal by remember { mutableStateOf(10000) }

    Box(modifier = Modifier.fillMaxSize()){

        when(activeScreen){
            0 -> PedometerScreen(steps = steps, goal = goal)
            1 -> DailyGoalScreen(goal = dailyGoal, onGoalChange = { newGoal -> dailyGoal = newGoal })
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { activeScreen = 0 }) {
                Text("Pedometer")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { activeScreen = 1 }) {
                Text("Daily Goal")
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

        TotalDistance(steps = steps, useMiles, goal = goal)
        //Spacer(modifier = Modifier.weight(.5f))

        StepCounter(steps = steps, goal = goal)
        //Spacer(modifier = Modifier.weight(1f))

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
    val distanceText = String.format("%.2f", distance)

    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Total Distance Today:",
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Color.Cyan,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp) // smaller spacing below
        )

        Text(
            text = "$distanceText $unitText",
            fontSize = 16.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Color.Cyan,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 0.dp) // no extra top padding
        )

        Text(
            text = "${(progress* 100).toInt()}% to daily goal",
            fontSize = 16.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Color.Cyan
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
fun DailyGoalScreen(goal: Int, onGoalChange: (Int) -> Unit){
    var dailyGoal by remember{ mutableStateOf(10000) }
    var inputText by remember {mutableStateOf("")}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = "Daily Goal: $dailyGoal steps",
                fontSize = 24.sp,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter new daily goal:", color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("e.g. 12000") },
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
                        onGoalChange(newGoal)
                        inputText = ""
                    }
                }
            ) {
                Text("Set Goal")
            }
        }
    }
}
