package com.example.buslinkdriver

import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.birjuvachhani.locus.Locus
import com.birjuvachhani.locus.extensions.isDenied
import com.birjuvachhani.locus.extensions.isFatal
import com.birjuvachhani.locus.extensions.isPermanentlyDenied
import com.birjuvachhani.locus.extensions.isSettingsDenied
import com.birjuvachhani.locus.extensions.isSettingsResolutionFailed
import com.example.buslinkdriver.util.extensions.update
import com.example.common.data.Buses
import com.example.common.data.BusesItem
import com.example.common.getBusesInfo
import com.example.common.sendDataToWebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class Event {
    data class SelectItem(val it: BusesItem) : Event()
    data class StartTracking(val context: Context) : Event()
    object SwitchMapShowing: Event()
}

data class BusLinkDriversState(
    val buses: Buses = Buses(),
    val selectedBuss: BusesItem? = null,
    val location: Location? = null,
    val isMapShowing: Boolean = true,
    val isTracking: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(BusLinkDriversState())
    val state: State<BusLinkDriversState> = _state

    init {
        getBusesInfo {
            _state.update { copy(buses = it) }
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.SelectItem -> _state.update { copy(selectedBuss = event.it) }
            is Event.StartTracking -> {
                if (state.value.selectedBuss == null){
                    Toast.makeText(event.context, "You need to select what bus you are driving.", Toast.LENGTH_SHORT).show()
                    return
                }
                if (state.value.isTracking) {
                    Locus.stopLocationUpdates()
                    sendDataToWebSocket(state.value.selectedBuss!!.bus_num, "-1")
                    _state.update { copy(isTracking = false, isMapShowing = true) }
                } else {
                    Locus.startLocationUpdates(context = event.context).observeForever { result ->
                        _state.update { copy(isTracking = true, isMapShowing = false, location = location) }

                        result.location?.let { location ->
                            _state.update { copy(location = location) }
                            sendDataToWebSocket(
                                state.value.selectedBuss!!.bus_num,
                                "${location.longitude} ${location.latitude}"
                            )
                        }
                        result.error?.let { error ->
                            _state.update { copy(isTracking = false, isMapShowing = true) }
                            val error = when {
                                error.isDenied -> "Permission denied"
                                error.isPermanentlyDenied -> "Permission is permanently denied"
                                error.isFatal -> "Something else went wrong!"
                                error.isSettingsDenied -> " Settings resolution denied by the user "
                                error.isSettingsResolutionFailed -> "Settings resolution failed!"
                                else -> "Something went wrong"
                            }
                            Toast.makeText(event.context, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
            Event.SwitchMapShowing -> _state.update { copy(isMapShowing = !isMapShowing) }
        }
    }

}


