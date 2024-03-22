package com.example.buslinkdriver

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buslinkdriver.ui.theme.BusLinkDriverTheme
import com.example.common.data.BusesItem
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("MutableCollectionMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapboxOptions.accessToken = stringResource(id = R.string.mapbox_public_token)
            BusLinkDriverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App(viewModel: MainViewModel = viewModel()) {
    val state = viewModel.state.value
    val context = LocalContext.current

    Column {
        AnimatedVisibility(visible = state.isMapShowing) {
            Box(
                modifier = Modifier
                    .background(Color.Blue)
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                mapItem()
            }
        }
        BottomSheet()
    }
}

@Composable
fun mapItem() {
    MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState = MapViewportState().apply {
            setCameraOptions {
                zoom(2.0)
                center(Point.fromLngLat(-98.0, 39.5))
                pitch(0.0)
                bearing(0.0)
            }
        },
    )

}


@Composable
fun BottomSheet(viewModel: MainViewModel = viewModel()) {

    val state = viewModel.state.value
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            if (state.isTracking) {
                                viewModel.onEvent(Event.SwitchMapShowing)
                            }
                        }
                    }
                    .background(Color.White)
                    .fillMaxWidth()
                    .height(64.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(4.dp)
                        .width(50.dp)
                        .background(Color.LightGray, RoundedCornerShape(100.dp))
                )
                Text(text = "Choose Bus")
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .height(2.dp)
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(100.dp))
                )

            }
            LazyColumn {
                items(state.buses) {
                    BusItem(
                        it = it,
                        showDetails = state.isTracking,
                        isSelected = it == state.selectedBuss
                    ) {
                        viewModel.onEvent(Event.SelectItem(it))
                    }
                }
            }
        }
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    onClick = {
                        viewModel.onEvent(Event.StartTracking(context))
                    }
                ) {
                    Text(
                        text = if (state.isTracking) "Stopppp!!" else "Start Tracking",
                    )
                }
            }
        }
    }
}


@Composable
fun BusItem(
    viewModel: MainViewModel = viewModel(),
    it: BusesItem,
    showDetails: Boolean = true,
    isSelected: Boolean = true,
    click: () -> Unit
) {
    val state = viewModel.state.value

    AnimatedVisibility(visible = !(showDetails && !isSelected)) {
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
                    Text(text = state.location?.latitude.toString())
                    Text(text = state.location?.longitude.toString())
                    Text(text = "Schedule")
                    Text(text = it.to)
                    Text(text = it.from)
                }
            }
        }
    }
}

