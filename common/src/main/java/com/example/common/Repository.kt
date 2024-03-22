package com.example.common

import android.util.Log
import com.example.common.data.Buses
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

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


fun sendDataToWebSocket(bus_id: Int, s: String) {
    val database = Firebase.database
    val myRef = database.getReference(bus_id.toString())
    myRef.setValue(s)
}

fun readFromWebSocket(function: (it: String) -> Unit) {
    val database = Firebase.database
    val myRef = database.getReference("132")

    myRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val value = dataSnapshot.getValue<String>()
            Log.d("MITOTEST", "Value is: $value")

            function(value.toString())
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("MITOTEST", "Failed to read value.", error.toException())
        }
    })
}