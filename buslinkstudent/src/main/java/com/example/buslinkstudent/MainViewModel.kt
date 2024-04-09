package com.example.buslinkstudent

import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.util.extensions.BusItem
import com.example.common.util.extensions.convertToPoints
import com.example.common.util.extensions.optimizeRoute
import com.example.common.util.extensions.update
import com.example.common.util.startTracking
import com.example.common.util.stopTracking
import com.example.common.util.getBusesInfo
import com.example.common.util.sendDataToWebSocket
import com.mapbox.geojson.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class Event {
    data class SelectItem(val it: BusItem) : Event()
    data class StartTracking(val context: Context) : Event()
    object SwitchMapShowing : Event()
}

data class BusLinkDriversState(
    val buses: List<BusItem> = listOf(),
    val selectedBuss: BusItem? = null,
    val location: Location? = null,
    val isMapShowing: Boolean = true,
    val isTracking: Boolean = false,
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(BusLinkDriversState())
    val state: State<BusLinkDriversState> = _state

    init {

        getBusesInfo {
            it.forEach { bus ->
                optimizeRoute(
                    bus.coords.convertToPoints()
                ) { newRoute ->
                    BusItem(
                        bus = bus.bus,
                        bus_num = bus.bus_num,
                        coords = bus.coords.convertToPoints(),
                        from = bus.from,
                        stops = bus.stops,
                        route = if(bus.bus.last() == 'l') newRoute.plusElement(Point.fromLngLat(5.7481969, 34.8455368)) else newRoute,
                        color = null,
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
                    if (state.value.selectedBuss == null) {
                        Toast.makeText(
                            event.context,
                            "You need to select what bus you are driving.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }
                    if (state.value.isTracking) {
                        _state.update { copy(isTracking = false, isMapShowing = true) }
                        stopTracking(state.value.selectedBuss!!.bus_num)
                    } else {
                        _state.update {
                            copy(
                                isTracking = true,
                                isMapShowing = false,
                                location = location
                            )
                        }

                        startTracking(
                            event.context,
                            err = { err ->
                                _state.update { copy(isTracking = false, isMapShowing = true) }
                                Toast.makeText(event.context, err, Toast.LENGTH_SHORT).show()
                            },
                            update = { location ->
                                _state.update { copy(location = location) }
                                viewModelScope.launch(Dispatchers.IO) {
                                    sendDataToWebSocket(
                                        state.value.selectedBuss!!.bus_num,
                                        "${location.longitude} ${location.latitude}"
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Event.SwitchMapShowing -> _state.update { copy(isMapShowing = !isMapShowing) }
        }
    }
}


