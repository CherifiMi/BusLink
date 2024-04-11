package com.example.buslinkstudent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buslinkstudent.theme.BusLinkStudentTheme
import com.example.common.util.readFromWebSocket
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.plugin.animation.camera
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(MapboxExperimental::class)
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

                    mapItem()

                }
            }
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun mapItem(viewModel: MainViewModel = viewModel()) {

    val state = viewModel.state.value

    val routs = state.buses.map { it.coords }

    MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState = MapViewportState().apply {
            setCameraOptions {
                zoom(12.0)
                center(Point.fromLngLat(5.7481969, 34.8455368))
                pitch(0.0)
                bearing(0.0)
            }
        },
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
            val alphaValue = if (selectedBusNum == null || selectedBusNum == state.buses[i].bus_num) 1f else 0f

            val black = Color(0f, 0f, 0f, alphaValue).toArgb()
            val white = Color(1f, 1f, 1f, alphaValue).toArgb()
            val randomColor = state.buses[i].color!!.toArgb()

            PolylineAnnotation(
                points = state.buses[i].route,
                lineJoin = LineJoin.ROUND,
                lineBorderColorInt = black,
                lineBorderWidth = 2.0,
                lineColorInt = randomColor,
                lineWidth = 8.0,
                lineBlur = 1.0
            )

            it.forEach { point ->
                CircleAnnotation(
                    point = point,
                    circleRadius = 8.0,
                    circleColorInt = black,
                )
                CircleAnnotation(
                    point = point,
                    circleRadius = 6.0,
                    circleColorInt = randomColor,
                )
                CircleAnnotation(
                    point = point,
                    circleRadius = 3.0,
                    circleColorInt = white,
                )
            }
            CircleAnnotation(
                point = Point.fromLngLat(5.7481969, 34.8455368),
                circleRadius = 17.0,
                circleColorInt = black,
            )
            CircleAnnotation(
                point = Point.fromLngLat(5.7481969, 34.8455368),
                circleRadius = 13.0,
                circleColorInt = white,
            )
        }
    }
}