package com.example.buslinkdriver

import android.provider.CallLog.Locations
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.common.data.Buses
import com.example.common.data.BusesItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class Event{
    //data class Order(val noteOrder: NoteOrder): NotesEvent()
    object RestoreNote: Event()
}

data class BusLinkDriversState(
    val buses: Buses = Buses(),
    val selectedBuss: BusesItem? = null,
    val location : Locations? = null
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(BusLinkDriversState())
    val state: State<BusLinkDriversState> = _state

    fun onEvent(event: Event) {
        /*_state.value = state.value.copy(
            isOrderSectionVisible = !state.value.isOrderSectionVisible
        )*/
        when(event) {
            Event.RestoreNote -> TODO()
        }
    }

}


