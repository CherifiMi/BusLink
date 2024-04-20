package com.example.common.util

import android.content.Context
import android.location.Location
import android.util.Log
import com.birjuvachhani.locus.Locus
import com.birjuvachhani.locus.extensions.isDenied
import com.birjuvachhani.locus.extensions.isFatal
import com.birjuvachhani.locus.extensions.isPermanentlyDenied
import com.birjuvachhani.locus.extensions.isSettingsDenied
import com.birjuvachhani.locus.extensions.isSettingsResolutionFailed
import com.example.common.data.Buses
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun getBusesInfo(f:(it:  Buses)-> Unit){
    val db = Firebase.firestore
    db.collection("BusDB")
        .get()
        .addOnSuccessListener { result ->
            val json = result.documents[0].data?.get("info").toString()
            Gson().fromJson(json, Buses::class.java).let {
                f(it)
            }
        }
}


suspend fun sendDataToWebSocket(bus_id: Int, s: String) = withContext(Dispatchers.IO){
    val database = Firebase.database
    val myRef = database.getReference(bus_id.toString())
    myRef.setValue(s)
}

suspend fun readFromWebSocket(bus_id: Int,function: (it: String) -> Unit) = withContext(Dispatchers.IO){
    val database = Firebase.database
    val myRef = database.getReference(bus_id.toString())

    myRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val value = dataSnapshot.getValue<String>()
            Log.d("MITOTEST", "Value is: $value")

            function(value.toString())
            //Log.d("MITOTEST", value.toString())
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("MITOTEST", "Failed to read value.", error.toException())
        }
    })
}

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

suspend fun stopTracking(busId: Int) = withContext(Dispatchers.IO) {
    Locus.stopLocationUpdates()
    sendDataToWebSocket(busId, "-1")
}