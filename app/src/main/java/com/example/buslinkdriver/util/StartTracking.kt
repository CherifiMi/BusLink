package com.example.buslinkdriver.util

import android.content.Context
import android.location.Location
import com.birjuvachhani.locus.Locus
import com.birjuvachhani.locus.extensions.isDenied
import com.birjuvachhani.locus.extensions.isFatal
import com.birjuvachhani.locus.extensions.isPermanentlyDenied
import com.birjuvachhani.locus.extensions.isSettingsDenied
import com.birjuvachhani.locus.extensions.isSettingsResolutionFailed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun startTracking(context: Context, update: (location: Location) -> Unit, err: (err: String) -> Unit){
        Locus.startLocationUpdates(context = context).observeForever { result ->
            result.location?.let { location ->
                update(location)
            }
            result.error?.let { error ->
                val err = when {
                    error.isDenied -> "Permission denied"
                    error.isPermanentlyDenied -> "Permission is permanently denied"
                    error.isFatal -> "Something else went wrong!"
                    error.isSettingsDenied -> " Settings resolution denied by the user "
                    error.isSettingsResolutionFailed -> "Settings resolution failed!"
                    else -> "Something went wrong"
                }
                err(err)
            }
        }
    }