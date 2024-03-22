package com.example.buslinkdriver

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.birjuvachhani.locus.Locus
import com.birjuvachhani.locus.extensions.isDenied
import com.birjuvachhani.locus.extensions.isFatal
import com.birjuvachhani.locus.extensions.isPermanentlyDenied
import com.birjuvachhani.locus.extensions.isSettingsDenied
import com.birjuvachhani.locus.extensions.isSettingsResolutionFailed
import com.example.common.data.Buses
import com.example.buslinkdriver.ui.theme.BusLinkDriverTheme
import com.example.common.data.BusesItem
import com.example.common.getBusesInfo
import com.example.common.sendDataToWebSocket
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("MutableCollectionMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BusLinkDriverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var items by remember {
                        mutableStateOf(Buses())
                    }
                    var showDetails by remember {
                        mutableStateOf(true)
                    }

                    getBusesInfo {
                        items = it
                    }

                    LazyColumn {
                        items(items) {
                            BusItem(it = it, showDetails = showDetails, isSelected = false) {
                                showDetails = !showDetails
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusItem(
    it: BusesItem,
    showDetails: Boolean = true,
    isSelected: Boolean = true,
    click: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
            .border(
                3.dp,
                if (isSelected && !showDetails) Color.Black else Color.White,
                RoundedCornerShape(15.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { click() }
            .clip(RoundedCornerShape(15.dp))
    ) {
        AnimatedVisibility(visible = showDetails) {
            Column(Modifier.padding(16.dp)) {
                Text(text = "Rout")
            }
        }
        Row {
            Image(
                painter = painterResource(id = R.drawable.side_icon),
                modifier = Modifier
                    .padding(16.dp)
                    .padding(top = 8.dp),
                contentDescription = null
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .padding(end = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = it.bus)
                    Text(text = it.bus_num.toString())
                }
                Spacer(modifier = Modifier.heightIn(16.dp))
                Text(
                    text = it.stops,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.heightIn(8.dp))
            }
        }
        AnimatedVisibility(visible = showDetails) {
            Column(Modifier.padding(16.dp)) {
                Text(text = "Location")
                Text(text = "212121122121")
                Text(text = "212121122121")
                Text(text = "Schedule")
                Text(text = it.to)
                Text(text = it.from)
            }
        }
    }
}

@Composable
fun App(viewModel: MainViewModel = viewModel()) {
    Box {
        var txt by remember { mutableStateOf("Start") }
        val context = LocalContext.current
        Button(onClick = {

            if (txt != "Start") {
                txt = "Start"
                Locus.stopLocationUpdates()
            } else {
                Locus.startLocationUpdates(context = context).observeForever { result ->
                    result.location?.let {
                        txt = "Stop ${it.longitude} ${it.latitude}"
                        sendDataToWebSocket(132, "${it.longitude} ${it.latitude}")
                    }
                    result.error?.let { error ->
                        txt = when {
                            error.isDenied -> "Permission denied"
                            error.isPermanentlyDenied -> "Permission is permanently denied"
                            error.isFatal -> "Something else went wrong!"
                            error.isSettingsDenied -> " Settings resolution denied by the user "
                            error.isSettingsResolutionFailed -> "Settings resolution failed!"
                            else -> "Something went wrong"
                        }
                    }
                }
            }
        }) {
            Text(text = txt)
        }
    }
}


