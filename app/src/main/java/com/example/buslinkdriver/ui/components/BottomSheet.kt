package com.example.buslinkdriver.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buslinkdriver.Event
import com.example.buslinkdriver.MainViewModel
import com.example.buslinkdriver.theme.UberFontFamily

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomSheet(viewModel: MainViewModel = viewModel()) {

    val state = viewModel.state.value
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            if (state.isTracking) {
                                viewModel.onEvent(Event.SwitchMapShowing)
                            }
                        }
                    }
                    .background(Color.White)
                    .fillMaxWidth()
                    .height(64.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(4.dp)
                        .width(50.dp)
                        .background(Color.LightGray, RoundedCornerShape(100.dp))
                )
                Box(Modifier.fillMaxWidth()) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !state.isTracking,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = "Choose Bus",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 24.sp
                            )
                        )
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.isTracking,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = "BusLink",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                            textAlign = TextAlign.Start,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 24.sp
                            )
                        )
                    }

                }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .height(2.dp)
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(100.dp))
                )
            }

            LazyColumn {
                items(state.buses) {
                    BusItem(
                        it = it,
                        showDetails = state.isTracking,
                        isSelected = it == state.selectedBuss
                    ) {
                        viewModel.onEvent(Event.SelectItem(it))
                    }
                }
                item { Spacer(modifier = Modifier.height(200.dp)) }
            }
        }
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    onClick = {
                        viewModel.onEvent(Event.StartTracking(context))
                    }
                ) {
                    Text(
                        text = if (state.isTracking) "Stopppp!!" else "Start Tracking",
                        style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 22.sp
                        )
                    )
                }
            }
        }
    }
}