package com.example.voiceassistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        val gson = Gson()
        userManager = UserManager(this, gson)
        setContent {
            MaterialTheme {
                LandingPage(userManager)
            }
        }
    }

    @Composable
    fun LandingPage(userManager: UserManager) {
        val users by remember { userManager.loadUsersState() }

        if (users.isEmpty()) {
            Information()
        } else {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                context.startActivity(Intent(context, LogInActivity::class.java))
                (context as? Activity)?.finish()
            }
        }
    }
}

@Composable
fun Information() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painterResource(id = R.drawable.musky),
            contentDescription = "",
            contentScale = ContentScale.FillBounds, // or some other scale
            modifier = Modifier.matchParentSize()
        )
        val context = LocalContext.current
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
            onClick = {
                context.startActivity(Intent(context, LogInActivity::class.java))
            }) {
            Text("Let's get started!")
        }
    }
}

@Preview
@Composable
fun InitialPreview() {
    Information()
}