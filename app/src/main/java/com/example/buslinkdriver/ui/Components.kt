package com.example.buslinkdriver.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buslinkdriver.Event
import com.example.buslinkdriver.MainViewModel
import com.example.buslinkdriver.R
import com.example.buslinkdriver.theme.UberFontFamily
import com.example.buslinkdriver.util.extensions.capitalizeFirst
import com.example.buslinkdriver.util.extensions.convertToPoints
import com.example.buslinkdriver.util.extensions.findNextAvailableTime
import com.example.buslinkdriver.util.extensions.stringToListStops
import com.example.common.data.BusesItem
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.plugin.animation.camera
import java.time.LocalTime

@Composable
fun mapItem(viewModel: MainViewModel = viewModel()) {

    val state = viewModel.state.value

    val (y, x) = if (state.selectedBuss?.bus_num == 132) state.selectedBuss?.coords?.first()
        ?: listOf(0.0, 0.0) else state.selectedBuss?.coords?.last() ?: listOf(0.0, 0.0)

    val routs = state.buses.map { it.coords.convertToPoints() }

    MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState = MapViewportState().apply {
            setCameraOptions {
                zoom(15.5)
                center(Point.fromLngLat(x, y))
                pitch(0.0)
                bearing(0.0)
            }
        },
    ) {
        MapEffect(key1 = x, key2 = y) { mapView ->
            state.selectedBuss?.coords?.apply {
                val polygon = Polygon.fromLngLats(listOf(this.convertToPoints()))
                val cameraPosition =
                    mapView.mapboxMap.cameraForGeometry(
                        polygon,
                        EdgeInsets(50.0, 50.0, 50.0, 50.0),
                    )
                mapView.camera.flyTo(cameraPosition)
            }
        }

        routs.forEach {

            PolylineAnnotation(
                points = it,
                lineJoin = LineJoin.ROUND,
                lineBorderColorInt = Color.Black.toArgb(),
                lineBorderWidth = 2.0,
                lineColorInt = Color.Black.toArgb(),
                lineWidth = 8.0,
            )

            it.forEach { point ->
                CircleAnnotation(
                    point = point,
                    circleRadius = 10.0,
                    circleColorInt = Color.Black.toArgb(),
                )
                CircleAnnotation(
                    point = point,
                    circleRadius = 5.0,
                    circleColorInt = Color.White.toArgb(),
                )
            }
        }

    }
}

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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BusItem(
    viewModel: MainViewModel = viewModel(),
    it: BusesItem,
    showDetails: Boolean = true,
    isSelected: Boolean = true,
    click: () -> Unit
) {
    val state = viewModel.state.value

    val animatedBorderColor: Color by animateColorAsState(
        targetValue = if (isSelected && !showDetails) Color.Black else Color.White,
        label = ""
    )

    AnimatedVisibility(visible = !(showDetails && !isSelected)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp)
                .border(
                    3.dp,
                    animatedBorderColor,
                    RoundedCornerShape(15.dp)
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { click() }
                .clip(RoundedCornerShape(15.dp))
        ) {
            AnimatedVisibility(visible = showDetails) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Rout", style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 24.sp
                        )
                    )
                }
            }
            Row {
                Image(
                    painter = painterResource(id = R.drawable.side_icon),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .padding(start = 16.dp, end = 4.dp, top = 8.dp),
                    contentDescription = null
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .padding(end = 16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = it.bus.capitalizeFirst(), style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = it.bus_num.toString(), style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.heightIn(10.dp))
                    val animatedTextLines: Float by animateFloatAsState(if (showDetails && isSelected) 10f else 1f)
                    Text(
                        text = it.stops.capitalizeFirst().stringToListStops().joinToString("->"),
                        maxLines = animatedTextLines.toInt(),
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.heightIn(8.dp))
                }
            }
            AnimatedVisibility(visible = showDetails) {
                Column(Modifier.padding(16.dp)) {

                    //---
                    Text(
                        text = "Schedule", style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 24.sp
                        )
                    )
                    Spacer(modifier = Modifier.heightIn(32.dp))

                    val (start, stop) = state.selectedBuss?.bus?.capitalizeFirst()?.split(",")
                        ?: listOf()
                    Text(
                        text = "$start->$stop",
                        color = Color.DarkGray,
                        textDecoration = TextDecoration.Underline,
                        style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    )
                    TimeList(it.from.stringToListStops())

                    Spacer(modifier = Modifier.heightIn(8.dp))

                    Text(
                        text = "$stop->$start",
                        color = Color.DarkGray,
                        textDecoration = TextDecoration.Underline,
                        style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    )
                    TimeList(it.to.stringToListStops())
                    Spacer(modifier = Modifier.heightIn(52.dp))

                    //---

                    Text(
                        text = "Location", style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 24.sp
                        )
                    )
                    Spacer(modifier = Modifier.heightIn(32.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "Latitude: ",
                            color = Color.DarkGray,
                            textDecoration = TextDecoration.Underline,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        )
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .offset(y = 4.dp),
                            colors = CardDefaults.cardColors(
                                Color.LightGray
                            )
                        ) {
                            Text(
                                text = "${state.location?.latitude?.toString() ?: "00.000000"}°",
                                modifier = Modifier
                                    .padding(2.dp)
                                    .padding(horizontal = 4.dp),
                                style = TextStyle(
                                    fontFamily = UberFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 17.sp
                                )
                            )
                        }

                    }

                    Spacer(modifier = Modifier.heightIn(8.dp))

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "Longitude: ",
                            color = Color.DarkGray,
                            textDecoration = TextDecoration.Underline,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        )
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .offset(y = 4.dp),
                            colors = CardDefaults.cardColors(
                                Color.LightGray
                            )
                        ) {
                            Text(
                                text = "${state.location?.longitude?.toString() ?: "00.000000"}°",
                                modifier = Modifier
                                    .padding(2.dp)
                                    .padding(horizontal = 4.dp),
                                style = TextStyle(
                                    fontFamily = UberFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 17.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeList(times: List<String>) {
    val currentTime = LocalTime.now()

    FlowRow(verticalArrangement = Arrangement.Bottom) {
        for (time in times) {
            val isNearestTime = time == findNextAvailableTime(times, currentTime)

            TimeItem(
                time = time,
                isNearestTime = isNearestTime
            )
        }
    }
}

@Composable
fun TimeItem(time: String, isNearestTime: Boolean) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .offset(y = if (!isNearestTime) (4).dp else 0.dp),
        colors = CardDefaults.cardColors(
            if (isNearestTime) Color.LightGray else Color.White
        )
    ) {
        Text(
            text = time,
            modifier = Modifier
                .padding(if (isNearestTime) 2.dp else 0.dp)
                .padding(horizontal = if (isNearestTime) 4.dp else 0.dp),
            fontSize = if (isNearestTime) 18.sp else 16.sp,
            fontWeight = if (isNearestTime) FontWeight.Medium else FontWeight.Medium,
            fontFamily = UberFontFamily
        )
    }
}