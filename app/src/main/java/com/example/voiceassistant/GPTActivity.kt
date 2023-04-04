package com.example.voiceassistant

import android.graphics.Paint.Align
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
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
            modifier = Modifier
                .matchParentSize()
                .zIndex(0f)
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
                        backgroundColor = Color.Transparent,
                        contentColor = Color.White,
                        title = { Text(text = "User Management") },
                        navigationIcon = {
                            IconButton(
                                onClick = {
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
            .padding(16.dp),
    ) {
        Text(text = "Users", modifier = Modifier.padding(bottom = 16.dp))
        LazyColumn {
            items(users) { user ->
                val showDialog = remember { mutableStateOf(false) }

                if (showDialog.value) {
                    UserEditDialog(user = user, onCloseDialog = { showDialog.value = false })
                }

                ListItem(
                    text = { Text(text = user.username) },
                    modifier = Modifier.clickable {
                        showDialog.value = true
                    }
                )
                Divider()
            }
        }
    }
}

@Composable
fun UserEditDialog(user: User, onCloseDialog: () -> Unit) {
    val (newUsername, setNewUsername) = rememberSaveable { mutableStateOf(user.username) }
    val (newApiKey, setNewApiKey) = rememberSaveable { mutableStateOf(user.apiKey) }
    val (currentPassword, setCurrentPassword) = rememberSaveable { mutableStateOf("") }
    val (newPassword, setNewPassword) = rememberSaveable { mutableStateOf("") }
    val (validateNewPassword, setValidateNewPassword) = rememberSaveable { mutableStateOf("") }
    var currentPasswordHidden by rememberSaveable { mutableStateOf(true) }
    var newPasswordHidden by rememberSaveable { mutableStateOf(true) }
    var validatePasswordHidden by rememberSaveable { mutableStateOf(true) }
    var apiKeyHidden by rememberSaveable { mutableStateOf(true) }

    AlertDialog(
        backgroundColor = Color.DarkGray,
        contentColor = Color.White,
        onDismissRequest = { onCloseDialog() },
        title = { Text("Edit User") },

        text = {
            Column {
                OutlinedTextField(
                    value = newUsername,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        unfocusedBorderColor = Color.Cyan,
                        cursorColor = Color.White,
                        backgroundColor = Color.DarkGray,
                        disabledLabelColor = Color.Transparent,
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.Cyan,
                    ),
                    onValueChange = { newUsername ->
                        setNewUsername(newUsername.take(16))
                    },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    label = { Text("New Username") },
                    trailingIcon = {
                        Icon(Icons.Default.Clear,
                            contentDescription = "clear text",
                            modifier = Modifier
                                .clickable {
                                    setNewUsername("")
                                }
                        )
                    }
                )
                OutlinedTextField(
                    value = newApiKey,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        unfocusedBorderColor = Color.Cyan,
                        cursorColor = Color.White,
                        backgroundColor = Color.DarkGray,
                        disabledLabelColor = Color.Transparent,
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.Cyan,
                    ),
                    onValueChange = { newApiKey ->
                        setNewApiKey(newApiKey.take(1024))
                    },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    label = { Text("New Api Key") },
                    visualTransformation =
                    if (apiKeyHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { apiKeyHidden = !apiKeyHidden }) {
                            val visibilityIcon =
                                if (apiKeyHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            // Please provide localized description for accessibility services
                            val description = if (apiKeyHidden) "Show password" else "Hide password"
                            Icon(imageVector = visibilityIcon, contentDescription = description)
                        }
                    }
                )
                OutlinedTextField(
                    value = currentPassword,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        unfocusedBorderColor = Color.Cyan,
                        cursorColor = Color.White,
                        backgroundColor = Color.DarkGray,
                        disabledLabelColor = Color.Transparent,
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.Cyan,
                    ),
                    onValueChange = { newCurrentPassword ->
                        setCurrentPassword(newCurrentPassword.take(32))
                    },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    label = { Text("Current Password") },
                    visualTransformation =
                    if (currentPasswordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { currentPasswordHidden = !currentPasswordHidden }) {
                            val visibilityIcon =
                                if (currentPasswordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            // Please provide localized description for accessibility services
                            val description = if (currentPasswordHidden) "Show password" else "Hide password"
                            Icon(imageVector = visibilityIcon, contentDescription = description)
                        }
                    }
                )
                OutlinedTextField(
                    value = newPassword,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        unfocusedBorderColor = Color.Cyan,
                        cursorColor = Color.White,
                        backgroundColor = Color.DarkGray,
                        disabledLabelColor = Color.Transparent,
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.Cyan,
                    ),
                    onValueChange = { newNewPassword ->
                        setNewPassword(newNewPassword.take(32))
                    },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    label = { Text("New Password") },
                    visualTransformation =
                    if (newPasswordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordHidden = !newPasswordHidden }) {
                            val visibilityIcon =
                                if (newPasswordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            // Please provide localized description for accessibility services
                            val description = if (newPasswordHidden) "Show password" else "Hide password"
                            Icon(imageVector = visibilityIcon, contentDescription = description)
                        }
                    }
                )
                OutlinedTextField(
                    value = validateNewPassword,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        unfocusedBorderColor = Color.Cyan,
                        cursorColor = Color.White,
                        backgroundColor = Color.DarkGray,
                        disabledLabelColor = Color.Transparent,
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.Cyan,
                    ),
                    onValueChange = { newValidatePassword ->
                        setValidateNewPassword(newValidatePassword.take(32))
                    },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    label = { Text("Validate Password") },
                    visualTransformation =
                    if (validatePasswordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { validatePasswordHidden = !validatePasswordHidden }) {
                            val visibilityIcon =
                                if (validatePasswordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            // Please provide localized description for accessibility services
                            val description = if (validatePasswordHidden) "Show password" else "Hide password"
                            Icon(imageVector = visibilityIcon, contentDescription = description)
                        }
                    }
                )
            }
        },
        buttons = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Cyan,
                        contentColor = Color.Black),
                    onClick = {
                        // TODO: Validate password and update user data (username, API key, password)
                        onCloseDialog()
                    }
                ) {
                    Text("Update User Info")
                }
                Spacer(modifier = Modifier.padding(8.dp))
            }
        },
    )
}
