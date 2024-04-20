package com.example.common.util.extensions

import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.optimization.v1.MapboxOptimization
import com.mapbox.api.optimization.v1.models.OptimizationResponse
import com.mapbox.geojson.Point
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.sqrt

fun optimizeRoute(oldRoute: List<Point>, f:(newRoute: List<Point>)->Unit){

    val accessToken =
        "pk.eyJ1IjoiZGEtbWl0byIsImEiOiJjbHUydG85Z2EwdGE3Mm1ueDg5d2VzenlrIn0.coUuklypqsJ8srkSOy9xlw"

    val optimizedClient = MapboxOptimization.builder()
        .coordinates(oldRoute)
        .profile(DirectionsCriteria.PROFILE_DRIVING)
        .accessToken(accessToken)
        .roundTrip(false)
        .source(DirectionsCriteria.SOURCE_FIRST)
        .destination(DirectionsCriteria.DESTINATION_LAST)
        .steps(true)
        .build()

    optimizedClient.enqueueCall(object : Callback<OptimizationResponse> {
        override fun onResponse(
            call: Call<OptimizationResponse>,
            response: Response<OptimizationResponse>
        ) {
            response.body()!!.trips()!![0].legs()
                ?.map { it?.steps()!!.map { it.maneuver().location() } }?.flatten()
                ?.let { nr ->
                    f.invoke(nr)
                }

        }

        override fun onFailure(call: Call<OptimizationResponse>, t: Throwable) {
        }
    })
}

fun findClosest(myPoint: Point, listOfPoints: List<Point>): Point {
    var d = 999999999.0
    var i = 0
    listOfPoints.forEachIndexed { index, point ->
        val (pX, pY) = listOf(myPoint.longitude(), myPoint.latitude())
        val (psX, psY) = listOf(point.longitude(), point.latitude())

        val distance = sqrt((psY - pY) * (psY - pY) + (psX - pX) * (psX - pX))

        if (distance<d) {
            d = distance
            i = index
        }
    }
    return listOfPoints[i]
}

fun calculateDistance(
    p1: Point,
    p2: Point,
): Double {

    val latitude1 = p1.latitude()
    val longitude1 = p1.longitude()
    val latitude2 = p2.latitude()
    val longitude2 = p2.longitude()

    val earthRadius = 6371.0 // Earth's radius in kilometers

    // Convert latitude and longitude from degrees to radians
    val dLat = Math.toRadians(latitude2 - latitude1)
    val dLon = Math.toRadians(longitude2 - longitude1)
    val lat1 = Math.toRadians(latitude1)
    val lat2 = Math.toRadians(latitude2)

    // Haversine formula
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1) * Math.cos(lat2) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    val distance = earthRadius * c

    return distance
}