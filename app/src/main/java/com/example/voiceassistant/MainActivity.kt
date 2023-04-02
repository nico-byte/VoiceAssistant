package com.example.voiceassistant

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {MainActivityComposable()}
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
@Composable
fun MainActivityComposable() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current

        Button(onClick = {
            context.startActivity(Intent(context, LogInActivity::class.java))
        }) {
            Text(text = "Let's get started!")
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true)
@Composable
fun DefaultPreview() {
    MainActivityComposable()
}

data class User(val username: String, val password: String, val apiKey: String)

class AuthManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        with(sharedPreferences.edit()) {
            putString("username", user.username)
            putString("password", user.password)
            putString("apiKey", user.apiKey)
            apply()
        }
    }

    fun loadUser(): User? {
        val username = sharedPreferences.getString("username", null)
        val password = sharedPreferences.getString("password", null)
        val apiKey = sharedPreferences.getString("apiKey", null)

        return if (username != null && password != null && apiKey != null) {
            User(username, password, apiKey)
        } else {
            null
        }
    }
}