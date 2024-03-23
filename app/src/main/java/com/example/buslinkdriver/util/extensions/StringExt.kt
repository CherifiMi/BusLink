package com.example.buslinkdriver.util.extensions

import android.os.Build
import androidx.annotation.RequiresApi
import com.mapbox.geojson.Point
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun String.capitalizeFirst() =
    this.split(" ").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }

fun String.stringToListStops() = this.trim('[', ']').split(",")
fun List<List<Double>>.convertToPoints(): List<Point> {
    val points = mutableListOf<Point>()

    this.forEach {(y,x)->
        points.add(Point.fromLngLat(x, y))
    }

    return points.toList()
}

@RequiresApi(Build.VERSION_CODES.O)
fun findNextAvailableTime(times: List<String>, currentTime: LocalTime): String? {
    for (time in times) {
        val parsedTime = LocalTime.parse(
            if (time.trim().length == 5) time.trim() else "0${time.trim()}",
            DateTimeFormatter.ofPattern("HH:mm")
        )
        if (parsedTime.isAfter(currentTime)) {
            return time
        }
    }
    return times[0]
}