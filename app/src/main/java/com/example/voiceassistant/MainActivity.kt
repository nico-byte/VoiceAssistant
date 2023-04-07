package com.example.voiceassistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        val gson = Gson()
        userManager = UserManager(this, gson)

        setContent {
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
                Navigator(userManager)
            }
        }
    }
}

@Composable
fun Navigator(userManager: UserManager) {
    val navController = rememberNavController()
    val users by remember { userManager.loadUsersState() }

    val startDest: String = if (users.isEmpty()) {
        "Welcome"
    } else {
        "LogIn"
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
        composable("Notes") {
            Notes(navController, userManager)
        }
        composable("GPT") {
            GPT(navController, userManager) { navController.popBackStack("Notes", false) }
        }
    }
}

@Composable
fun Information(navController: NavHostController) {
    Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
        onClick = {
            navController.navigate("SignUp")
        }) {
        Text("Let's get started!")
    }
}

@Composable
fun RegistrationScreen(navController: NavHostController, userManager: UserManager) {
    val helper = Helper()
    val (username, setUsername) = rememberSaveable { mutableStateOf("") }
    val (password, setPassword) = rememberSaveable { mutableStateOf("") }
    var passwordHidden by rememberSaveable { mutableStateOf(true) }
    val (apiKey, setApiKey) = rememberSaveable { mutableStateOf("") }
    var apiKeyHidden by rememberSaveable { mutableStateOf(true) }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // username
        helper.unsafeTextField(
            value = username,
            onValueChange = { newUsername ->
                setUsername(newUsername.take(16))
            },
            label = "Enter Username",
            trailingIcon = {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "clear text",
                    modifier = Modifier
                        .clickable {
                            setUsername("")
                        }
                )
            })
        // password
        helper.safeTextField(
            value = password,
            onValueChange = { newPassword ->
                setPassword(newPassword.take(32))
            },
            label = "Enter Password",
            visualTransformation =
            if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                IconButton(onClick = { passwordHidden = !passwordHidden }) {
                    val visibilityIcon =
                        if (passwordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    // Please provide localized description for accessibility services
                    val description = if (passwordHidden) "Show password" else "Hide password"
                    Icon(imageVector = visibilityIcon, contentDescription = description)
                }
            })
        // api key
        helper.safeTextField(
            value = apiKey,
            onValueChange = { newApiKey ->
                setApiKey(newApiKey.take(1024))
            },
            label = "Enter API Key",
            visualTransformation =
            if (apiKeyHidden) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                IconButton(onClick = { apiKeyHidden = !apiKeyHidden }) {
                    val visibilityIcon =
                        if (apiKeyHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    // Please provide localized description for accessibility services
                    val description = if (apiKeyHidden) "Show api key" else "Hide api key"
                    Icon(imageVector = visibilityIcon, contentDescription = description)
                }
            })

        val uriHandler = LocalUriHandler.current
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
            onClick = {
                uriHandler.openUri("https://platform.openai.com/account/api-keys")
            }, shape = RoundedCornerShape(20.dp)
        ) {
            Text(text = "GET API KEY")
        }

        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
            onClick = {
                val newUser = User(username, password, apiKey)
                userManager.saveUser(newUser)
                navController.navigate("LogIn")
            }, shape = RoundedCornerShape(20.dp)
        ) {
            Text(text = "SIGN IN")
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController, userManager: UserManager) {
    val helper = Helper()
    val (username, setUsername) = rememberSaveable { mutableStateOf("") }
    val (password, setPassword) = rememberSaveable { mutableStateOf("") }
    var passwordHidden by rememberSaveable { mutableStateOf(true) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // username
        helper.unsafeTextField(
            value = username,
            onValueChange = { newUsername ->
                setUsername(newUsername.take(16))
            },
            label = "Enter Username",
            trailingIcon = {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "clear text",
                    modifier = Modifier
                        .clickable {
                            setUsername("")
                        }
                )
            })
        // password
        helper.safeTextField(
            value = password,
            onValueChange = { newPassword ->
                setPassword(newPassword.take(32))
            },
            label = "Enter Password",
            visualTransformation =
            if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                IconButton(onClick = { passwordHidden = !passwordHidden }) {
                    val visibilityIcon =
                        if (passwordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    // Please provide localized description for accessibility services
                    val description = if (passwordHidden) "Show password" else "Hide password"
                    Icon(imageVector = visibilityIcon, contentDescription = description)
                }
            })

        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
            onClick = {
                val users = userManager.loadUsers()
                val user = users.find { it.username == username && it.password == password }
                if (user != null) {
                    navController.navigate("Notes")
                }
            }, shape = RoundedCornerShape(20.dp)
        ) {
            Text(text = "LOG IN")
        }
    }
}

@Composable
fun Notes(navController: NavHostController, userManager: UserManager) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val users = userManager.loadUsers()
    val coroutineScope = rememberCoroutineScope()
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
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open Drawer")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            // TODO: Add action for adding something
                        }) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Add something",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar() },
            content = { padding ->
                Column(modifier = Modifier.padding(padding)) {
                    //TODO: Add UI for Notes and integrate API - Whisper
                }
            }
        )
    }
}

@Composable
fun GPT(navController: NavHostController, userManager: UserManager, onNotes: () -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val users = userManager.loadUsers()
    val coroutineScope = rememberCoroutineScope()
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
            bottomBar = { BottomNavigationBar() },
            content = { padding ->
                Column(modifier = Modifier.padding(padding)) {
                    //TODO: Add GPT UI and integrate API (Whisper and GPT)
                }
            }
        )
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
    val helper = Helper()
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
                helper.unsafeTextField(
                    value = newUsername,
                    onValueChange = { newUsername ->
                        setNewUsername(newUsername.take(16))
                    },
                    label = "New Username",
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
                helper.safeTextField(
                    value = newApiKey,
                    onValueChange = { newApiKey ->
                        setNewApiKey(newApiKey.take(1024))
                    },
                    label = "New API Key",
                    visualTransformation =
                    if (apiKeyHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        IconButton(onClick = { apiKeyHidden = !apiKeyHidden }) {
                            val visibilityIcon =
                                if (apiKeyHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            // Please provide localized description for accessibility services
                            val description = if (apiKeyHidden) "Show api key" else "Hide api key"
                            Icon(imageVector = visibilityIcon, contentDescription = description)
                        }
                    }
                )
                helper.safeTextField(
                    value = currentPassword,
                    onValueChange = { newCurrentPassword ->
                        setCurrentPassword(newCurrentPassword.take(32))
                    },
                    label = "Current Password",
                    visualTransformation =
                    if (currentPasswordHidden) PasswordVisualTransformation() else VisualTransformation.None,
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
                helper.safeTextField(
                    value = newPassword,
                    onValueChange = { newNewPassword ->
                        setNewPassword(newNewPassword.take(32))
                    },
                    label = "New Password",
                    visualTransformation =
                    if (newPasswordHidden) PasswordVisualTransformation() else VisualTransformation.None,
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
                helper.safeTextField(
                    value = validateNewPassword,
                    onValueChange = { newValidatePassword ->
                        setValidateNewPassword(newValidatePassword.take(32))
                    },
                    label = "Validate Password",
                    visualTransformation =
                    if (validatePasswordHidden) PasswordVisualTransformation() else VisualTransformation.None,
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

@Composable
fun BottomNavigationBar() {
    val items = listOf(
        NavigationItem.Help,
        NavigationItem.Mic,
        NavigationItem.Add,
    )
    BottomNavigation(
        backgroundColor = Color.DarkGray,
        contentColor = Color.White
    ) {
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.title) },
                label = { Text(text = item.title) },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.White.copy(0.4f),
                alwaysShowLabel = true,
                selected = false,
                onClick = {
                    /* Add code later */
                }
            )
        }
    }
}