package com.example.buslinkstudent

import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.util.extensions.BusItem
import com.example.common.util.extensions.convertToPoints
import com.example.common.util.extensions.optimizeRoute
import com.example.common.util.extensions.update
import com.example.common.util.startTracking
import com.example.common.util.getBusesInfo
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class Event {
    data class SelectItem(val it: BusItem) : Event()
    data class StartTracking(val context: Context) : Event()
    object SwitchMapShowing : Event()
    object GoToHome : Event()
    object GoToPuck : Event()
}

data class BusLinkDriversState @OptIn(MapboxExperimental::class) constructor(
    val buses: List<BusItem> = listOf(),
    val selectedBuss: BusItem? = null,
    val location: Location? = null,
    val isMapShowing: Boolean = true,
    val isTracking: Boolean = false,
    val mapViewportState: MapViewportState = MapViewportState(
        CameraState(
            Point.fromLngLat(5.7481969, 34.8455368),
            EdgeInsets(0.0,0.0,0.0,0.0),
            12.0,
            0.0,
            0.0
        )
    )
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(BusLinkDriversState())
    val state: State<BusLinkDriversState> = _state

    init {
        getBusesInfo {
            it.forEach { bus ->

                val randomColor = Color((0..255).random(),(0..255).random(),(0..255).random())

                optimizeRoute(
                    bus.coords.convertToPoints()
                ) { newRoute ->
                    BusItem(
                        bus = bus.bus,
                        bus_num = bus.bus_num,
                        coords = bus.coords.convertToPoints(),
                        from = bus.from,
                        stops = bus.stops,
                        route = newRoute,
                        color = randomColor,
                        to = bus.to,
                    ).let {
                        _state.update { copy(buses = buses + it) }
                    }
                }
            }
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.SelectItem -> _state.update { copy(selectedBuss = event.it) }
            is Event.StartTracking -> {
                viewModelScope.launch {
                    startTracking(
                        event.context,
                        err = { err ->
                            _state.update { copy(isTracking = false, isMapShowing = true) }
                            Toast.makeText(event.context, err, Toast.LENGTH_SHORT).show()
                        },
                        update = { location ->
                            _state.update { copy(location = location) }
                        }
                    )
                }
            }

            Event.SwitchMapShowing -> _state.update { copy(isMapShowing = !isMapShowing) }
            Event.GoToHome -> {
                val cameraOption = CameraOptions.Builder().center(Point.fromLngLat(5.7481969, 34.8455368)).zoom(12.0).bearing(0.0).pitch(0.0).build()
                state.value.mapViewportState.flyTo(cameraOption)
                _state.update { copy(selectedBuss = null) }
            }
            Event.GoToPuck -> {
                state.value.mapViewportState.transitionToFollowPuckState { state.value.mapViewportState.idle() }
            }
        }
    }
}


