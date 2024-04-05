package com.example.buslinkdriver.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buslinkdriver.MainViewModel
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

            val randomColor = Color(15f / 255, 19f / 255, 54f / 255, alphaValue).toArgb()
            val black = Color(0f, 0f, 0f, alphaValue).toArgb()
            val white = Color(1f, 1f, 1f, alphaValue).toArgb()

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