package com.example.buslinkdriver

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalContext
import com.birjuvachhani.locus.Locus
import com.birjuvachhani.locus.extensions.isDenied
import com.birjuvachhani.locus.extensions.isFatal
import com.birjuvachhani.locus.extensions.isPermanentlyDenied
import com.birjuvachhani.locus.extensions.isSettingsDenied
import com.birjuvachhani.locus.extensions.isSettingsResolutionFailed
import com.example.buslinkdriver.ui.theme.BusLinkDriverTheme
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

                    try {
                        val db = Firebase.firestore
                        db.collection("BusDB")
                            .get()
                            .addOnSuccessListener { result ->
                                txt = result.documents.get(0).data.toString()
                            }
                    }catch (e:Exception){
                        Log.e("MITOTEST", "ERR", e)
                    }

                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
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

fun sendDataToWebSocket(bus_id: Int, s: String) {
    val database = Firebase.database
    val myRef = database.getReference(bus_id.toString())

    myRef.setValue(s)

}

