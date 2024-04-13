package com.example.buslinkstudent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buslinkstudent.theme.BusLinkStudentTheme
import com.example.buslinkstudent.theme.UberFontFamily
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.DefaultSettingsProvider
import com.mapbox.maps.extension.compose.DefaultSettingsProvider.createDefault2DPuck
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.camera
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(MapboxExperimental::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapboxOptions.accessToken = stringResource(id = R.string.mapbox_public_token)
            BusLinkStudentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    /*var txt by remember { mutableStateOf("text") }
                    Text(text = txt)

                    runBlocking(Dispatchers.IO){
                        readFromWebSocket(132){ txt = it }
                    }*/
                    val scaffoldState = rememberBottomSheetScaffoldState()

                    Box {
                        BottomSheetScaffold(
                            scaffoldState = scaffoldState,
                            sheetPeekHeight = 144.dp,
                            sheetContent = { BottomSheetContent(scaffoldState) },
                            sheetContainerColor = Color.Transparent,
                            sheetDragHandle = { BottomSheetButtons() },
                            sheetShadowElevation = 0.dp
                        ) {
                            mapItem()
                        }

                        SplashScreen()
                    }
                }
            }
        }
    }

}


@Composable
fun SplashScreen(viewModel: MainViewModel = viewModel()) {

    val state = viewModel.state.value

    var showSplashScreen by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(state.buses){
        delay(2000)
        showSplashScreen = false
    }

    AnimatedVisibility(visible = showSplashScreen, exit = fadeOut()) {
        Column(
            Modifier
                .background(Color.Black)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                painter = painterResource(id = R.drawable.ic_students_foreground),
                contentDescription = null,
                tint = Color.Blue,
                modifier = Modifier.size(160.dp)
            )

            Text(
                text = "BusLink",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = UberFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp,
                    color = Color.Blue
                )
            )

        }
    }
}

@Composable
fun BottomSheetButtons() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            IconButton(onClick = { /*TODO*/ }, Modifier.size(48.dp)) {
                Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null)
            }
            IconButton(onClick = { /*TODO*/ }, Modifier.size(48.dp)) {
                Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null)
            }
        }
        IconButton(onClick = { /*TODO*/ }, Modifier.size(48.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_students_foreground),
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.BottomSheetContent(
    scaffoldState: BottomSheetScaffoldState,
    viewModel: MainViewModel = viewModel()
) {

    val state = viewModel.state.value
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.White)
                )
            )
    )

    LazyRow(
        Modifier
            .background(Color.White)
            .fillMaxWidth()
            .height(64.dp)
    ) {
        items(state.buses) {
            TextButton(onClick = { viewModel.onEvent(Event.SelectItem(it)) }) {
                Text(text = it.bus_num.toString(), color = it.color!!)
            }
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //scope.launch { scaffoldState.bottomSheetState.partialExpand() }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun mapItem(viewModel: MainViewModel = viewModel()) {

    val state = viewModel.state.value

    val routs = state.buses.map { it.coords }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(12.0)
            center(Point.fromLngLat(5.7481969, 34.8455368))
            pitch(0.0)
            bearing(0.0)
        }
    }

    val context = LocalContext.current

    MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        locationComponentSettings = DefaultSettingsProvider.defaultLocationComponentSettings(
            context,
            LocalDensity.current.density
        ).toBuilder()
            .setLocationPuck(createDefault2DPuck(withBearing = true))
            .setPuckBearingEnabled(true)
            .setPuckBearing(PuckBearing.HEADING)
            .setEnabled(true)
            .build()
    ) {
        MapEffect(state.selectedBuss) { mapView ->
            state.selectedBuss?.let {
                val polygon = Polygon.fromLngLats(listOf(it.route))
                val cameraPosition =
                    mapView.mapboxMap.cameraForGeometry(
                        polygon,
                        EdgeInsets(50.0, 50.0, 50.0, 50.0),
                    )
                mapView.camera.easeTo(cameraPosition)
            }
        }

        routs.forEachIndexed { i, it ->

            val selectedBusNum = state.selectedBuss?.bus_num
            val alphaValue =
                if (selectedBusNum == null || selectedBusNum == state.buses[i].bus_num) 1f else 0f
            val randomColor = state.buses[i].color!!.copy(alpha = alphaValue).toArgb()

            PolylineAnnotation(
                points = state.buses[i].route,
                lineJoin = LineJoin.ROUND,
                lineColorInt = randomColor,
                lineWidth = 8.0,
            )

            it.forEach { point ->
                CircleAnnotation(
                    point = point,
                    circleRadius = 7.0,
                    circleColorInt = randomColor,
                )
            }
        }


        LaunchedEffect(Unit) {
            delay(5000)
            mapViewportState.transitionToFollowPuckState()
        }

    }
}