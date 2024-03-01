package com.example.myapplication

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.CameraListener
import com.example.myapplication.utils.UDPManager

class MainActivity : ComponentActivity(), CameraListener{
    lateinit var udpManager: UDPManager;
    override fun onCreate(savedInstanceState: Bundle?) {
        udpManager = UDPManager.getInstance(this)
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

    override fun cameraChanged(i: Int) {
//        TODO("Not yet implemented")
    }

    override fun connectStateChanged(z: Boolean, str: String?) {
//        TODO("Not yet implemented")
    }

    override fun receiveBatteryData(i: Int) {
//        TODO("Not yet implemented")
    }

    override fun receiveImageData(bitmap: Bitmap?, i: Int) {
//        TODO("Not yet implemented")
    }

    override fun resolutionChanged(str: String?) {
//        TODO("Not yet implemented")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}