package com.example.common.util.extensions

import androidx.compose.ui.graphics.Color
import com.mapbox.geojson.Point

data class BusItem(
    val bus: String,
    val bus_num: Int,
    val coords: List<Point>,
    val from: String,
    val stops: String,
    val route: List<Point>,
    val color: Color?,
    val to: String
)