package com.example.buslinkdriver.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buslinkdriver.MainViewModel
import com.example.buslinkdriver.R
import com.example.buslinkdriver.theme.UberFontFamily
import com.example.common.util.extensions.BusItem
import com.example.common.util.extensions.capitalizeFirst
import com.example.common.util.extensions.stringToListStops

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BusItem(
    viewModel: MainViewModel = viewModel(),
    it: BusItem,
    showDetails: Boolean = true,
    isSelected: Boolean = true,
    click: () -> Unit
) {
    val state = viewModel.state.value

    val animatedBorderColor: Color by animateColorAsState(
        targetValue = if (isSelected && !showDetails) Color.Black else Color.White,
        label = ""
    )

    AnimatedVisibility(visible = !(showDetails && !isSelected)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp)
                .border(
                    3.dp,
                    animatedBorderColor,
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
                    Text(
                        text = "Rout", style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 24.sp
                        )
                    )
                }
            }
            Row {
                Image(
                    painter = painterResource(id = R.drawable.side_icon),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .padding(start = 16.dp, end = 4.dp, top = 8.dp),
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
                        Text(
                            text = it.bus.capitalizeFirst(), style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = it.bus_num.toString(), style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.heightIn(10.dp))
                    val animatedTextLines: Float by animateFloatAsState(if (showDetails && isSelected) 10f else 1f)
                    Text(
                        text = it.stops.capitalizeFirst().stringToListStops().joinToString("->"),
                        maxLines = animatedTextLines.toInt(),
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.heightIn(8.dp))
                }
            }
            AnimatedVisibility(visible = showDetails) {
                Column(Modifier.padding(16.dp)) {

                    //---
                    Text(
                        text = "Schedule", style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 24.sp
                        )
                    )
                    Spacer(modifier = Modifier.heightIn(32.dp))

                    val (start, stop) = state.selectedBuss?.bus?.capitalizeFirst()?.split(",")
                        ?: listOf()
                    Text(
                        text = "$start->$stop",
                        color = Color.DarkGray,
                        textDecoration = TextDecoration.Underline,
                        style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    )
                    TimeList(it.from.stringToListStops())

                    Spacer(modifier = Modifier.heightIn(8.dp))

                    Text(
                        text = "$stop->$start",
                        color = Color.DarkGray,
                        textDecoration = TextDecoration.Underline,
                        style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    )
                    TimeList(it.to.stringToListStops())
                    Spacer(modifier = Modifier.heightIn(52.dp))

                    //---

                    Text(
                        text = "Location", style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 24.sp
                        )
                    )
                    Spacer(modifier = Modifier.heightIn(32.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "Latitude: ",
                            color = Color.DarkGray,
                            textDecoration = TextDecoration.Underline,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        )
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .offset(y = 4.dp),
                            colors = CardDefaults.cardColors(
                                Color.LightGray
                            )
                        ) {
                            Text(
                                text = "${state.location?.latitude?.toString() ?: "00.000000"}°",
                                modifier = Modifier
                                    .padding(2.dp)
                                    .padding(horizontal = 4.dp),
                                style = TextStyle(
                                    fontFamily = UberFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 17.sp
                                )
                            )
                        }

                    }

                    Spacer(modifier = Modifier.heightIn(8.dp))

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "Longitude: ",
                            color = Color.DarkGray,
                            textDecoration = TextDecoration.Underline,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        )
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .offset(y = 4.dp),
                            colors = CardDefaults.cardColors(
                                Color.LightGray
                            )
                        ) {
                            Text(
                                text = "${state.location?.longitude?.toString() ?: "00.000000"}°",
                                modifier = Modifier
                                    .padding(2.dp)
                                    .padding(horizontal = 4.dp),
                                style = TextStyle(
                                    fontFamily = UberFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 17.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}