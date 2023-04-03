package com.example.voiceassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import com.google.gson.Gson


class GPTActivity : ComponentActivity() {
    private lateinit var userManager: UserManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gson = Gson()
        userManager = UserManager(this, gson)
        setContent {GPT(userManager)}
    }
}

@Composable
fun GPT(userManager: UserManager) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val users = userManager.loadUsers()
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painterResource(id = R.drawable.musky),
            contentDescription = "",
            contentScale = ContentScale.FillBounds, // or some other scale
            modifier = Modifier.matchParentSize().zIndex(0f)
        )
        ModalDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(users, onCloseDrawer = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                })
            },
            modifier = Modifier.zIndex(1f) // Set a higher zIndex for the ModalDrawer
        ) {
            Scaffold(
                backgroundColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        backgroundColor = Color.Cyan,
                        title = { Text(text = "User Management") },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Open Drawer")
                            }
                        }
                    )
                },
                content = { padding ->
                    Column(modifier = Modifier.padding(padding)) {
                        //TODO: Add GPT UI and integrate API (Whisper and GPT)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DrawerContent(users: List<User>, onCloseDrawer: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text(text = "Users", style = MaterialTheme.typography.h4, modifier = Modifier.padding(bottom = 16.dp))
        LazyColumn {
            items(users) { user ->
                ListItem(
                    text = { Text(text = user.username) },
                    secondaryText = { Text(text = user.apiKey) },
                    //TODO: Add a click listener to modify the user
                    modifier = Modifier.clickable {
                        //TODO: Modify the user data here
                        onCloseDrawer()
                    }
                )
                Divider()
            }
        }
    }
}