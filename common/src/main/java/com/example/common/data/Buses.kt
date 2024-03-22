package com.example.common.data

class Buses : ArrayList<BusesItem>()
data class BusesItem(
    val bus: String,
    val bus_num: Int,
    val coords: List<List<Double>>,
    val from: String,
    val stops: String,
    val to: String
)