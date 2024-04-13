package com.example.buslinkstudent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buslinkstudent.theme.BusLinkStudentTheme
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
import kotlinx.coroutines.launch

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

                    BottomSheetScaffold(
                        scaffoldState = scaffoldState,
                        sheetPeekHeight = 158.dp,
                        sheetContent = { BottomSheetContent(scaffoldState) }
                    ) {
                        mapItem()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.BottomSheetContent(scaffoldState: BottomSheetScaffoldState, viewModel: MainViewModel = viewModel() ) {

    val state = viewModel.state.value
    val scope = rememberCoroutineScope()

    LazyRow(
        Modifier
            .fillMaxWidth()
            .background(Color.Red)
            .height(128.dp)
    ) {
        items(state.buses){
            TextButton(onClick = { viewModel.onEvent(Event.SelectItem(it)) }) {
                Text(text = it.bus_num.toString(), color = it.color!!)
            }
        }
    }
    
    
    Column(
        Modifier
            .fillMaxWidth()
            .padding(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sheet content")
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                scope.launch { scaffoldState.bottomSheetState.partialExpand() }
            }
        ) {
            Text("Click to collapse sheet")
        }
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