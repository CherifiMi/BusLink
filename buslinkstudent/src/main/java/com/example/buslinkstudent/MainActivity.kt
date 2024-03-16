package com.example.buslinkstudent

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.buslinkstudent.ui.theme.BusLinkDriverTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BusLinkDriverTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var txt by remember { mutableStateOf("text") }
                    Text(text = txt)

                    readFromDB { txt = it }

                    /*val db = Firebase.firestore
                    db.collection("BusDB")
                        .get()
                        .addOnSuccessListener { result ->
                            txt = "MITO STUDENT" + result.documents.get(0).data.toString()
                        }*/
                }
            }
        }
    }
}

fun readFromDB(function: (it: String) -> Unit) {
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