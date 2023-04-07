package com.example.voiceassistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.voiceassistant.ui.theme.VoiceAssistantTheme
import com.google.gson.Gson

class Navigation : ComponentActivity() {
    private lateinit var userManager: UserManager
    @Composable
    fun NavigationView() {
        val gson = Gson()
        userManager = UserManager(this, gson)
        val navController = rememberNavController()
        val users by remember { userManager.loadUsersState() }
        var startDest:String? = null

        if (users.isEmpty()) {
            startDest = "Welcome"
        } else {
            startDest = "LogIn"
            }

        NavHost(navController = navController, startDestination = startDest) {
            composable("Welcome") {
                Information(navController)
            }
            composable("SignUp") {
                RegistrationScreen(navController, userManager)
            }
            composable("LogIn") {
                LoginScreen(navController, userManager)
            }
            composable("GPT") {
                GPT(navController, userManager) { navController.popBackStack("Notes", false) }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VoiceAssistantTheme {
        Greeting("Android")
    }
}