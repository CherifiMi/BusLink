package com.example.buslinkdriver.util.extensions

import androidx.compose.ui.graphics.Color
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.optimization.v1.MapboxOptimization
import com.mapbox.api.optimization.v1.models.OptimizationResponse
import com.mapbox.geojson.Point
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
