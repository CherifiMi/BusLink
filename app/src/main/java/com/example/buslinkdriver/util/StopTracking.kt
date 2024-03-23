package com.example.buslinkdriver.util

import com.birjuvachhani.locus.Locus
import com.example.common.sendDataToWebSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun stopTracking(busId: Int) = withContext(Dispatchers.IO) {
    Locus.stopLocationUpdates()
    sendDataToWebSocket(busId, "-1")
}