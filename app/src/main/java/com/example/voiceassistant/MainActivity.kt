package com.example.voiceassistant

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aallam.openai.api.BetaOpenAI
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    private lateinit var userManager: UserManager
    private lateinit var chatHistoryManager: ChatHistoryManager
    private lateinit var models: Models

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(BetaOpenAI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        val gson = Gson()
        userManager = UserManager(this, gson)
        chatHistoryManager = ChatHistoryManager(this)
        models = Models(chatHistoryManager, userManager, this)

        setContent {
            this.window.statusBarColor = Color.Black.toArgb()
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
                Navigator(userManager, chatHistoryManager, models)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@ExperimentalAnimationApi
@Composable
fun Navigator(userManager: UserManager, chatHistoryManager: ChatHistoryManager, models: Models) {
    val navController = rememberNavController()
    val users by remember { userManager.loadUsersState() }

    val startDest: String = if (users.isEmpty()) {
        "Welcome"
    } else {
        "LogIn"
    }

    NavHost(navController = navController, startDestination = startDest) {
        composable("Welcome") {
            Information(navController) }
        composable("SignUp") {
            RegistrationScreen(navController, userManager)
        }
        composable("LogIn") {
            LoginScreen(navController, userManager)
        }
        composable("Notes") {
            val isButton1Enabled = false
            val isButton2Enabled = true
            Notes(navController, userManager, isButton1Enabled, isButton2Enabled)
        }
        composable("GPT") {
            val isButton1Enabled = true
            val isButton2Enabled = false
            GPT(navController, userManager, isButton1Enabled, isButton2Enabled, chatHistoryManager)
            { navController.popBackStack("Notes", false) }
        }
        composable("Playground") {
            val isButton1Enabled = true
            val isButton2Enabled = false
            FeaturePlayground(navController, userManager, isButton1Enabled, isButton2Enabled,
                chatHistoryManager, models)
            { navController.popBackStack("Notes", false) }
        }
    }
}

@Composable
fun Information(navController: NavHostController) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
            onClick = {
                navController.navigate("SignUp")
            }) {
            Text("Let's get started!")
        }
    }
}

@ExperimentalAnimationApi
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
fun Notes(navController: NavHostController, userManager: UserManager,
          isButton1Enabled: Boolean, isButton2Enabled: Boolean) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val user = userManager.loadUsers()
    val coroutineScope = rememberCoroutineScope()
    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(user, onCloseDrawer = {
                coroutineScope.launch {
                    delay(timeMillis = 250)
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
                    title = { Text(text = "Note Manager") },
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
                                Icons.Filled.Info,
                                contentDescription = "App informations",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                                   FloatingActionButton(onClick = { /*TODO*/ }) {
                                       Icon(Icons.Filled.Mic, "")
                                   }
            },
            floatingActionButtonPosition = FabPosition.End,
            isFloatingActionButtonDocked = true,
            bottomBar = { BottomNavigationBar(navController, isButton1Enabled, isButton2Enabled,
            showNotesButton = false, showGPTButton = true) },
            content = { padding ->
                Column(modifier = Modifier.padding(padding)) {
                    //TODO: Add UI for Notes and integrate API - Whisper
                }
            }
        )
    }
}

@OptIn(BetaOpenAI::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun GPT(navController: NavHostController, userManager: UserManager,
        isButton1Enabled: Boolean, isButton2Enabled: Boolean, chatHistoryManager: ChatHistoryManager,
        onNotes: () -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val user = userManager.loadUsers()
    val coroutineScope = rememberCoroutineScope()
    val (username, setUsername) = rememberSaveable { mutableStateOf("") }

    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(user, onCloseDrawer = {
                coroutineScope.launch {
                    delay(timeMillis = 250)
                    drawerState.close()
                }
            })
        },
        modifier = Modifier.zIndex(1f) // Set a higher zIndex for the ModalDrawer
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Scaffold(
                backgroundColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        backgroundColor = Color.Transparent,
                        contentColor = Color.White,
                        title = { Text(text = "Chat GPT 3.5 Turbo") },
                        navigationIcon = {
                            IconButton(
                                onClick = {
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
                                navController.navigate("Playground")
                            }) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = "App informations",
                                    tint = Color.White
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Mic, "")
                    }
                },
                floatingActionButtonPosition = FabPosition.End,
                isFloatingActionButtonDocked = true,
                bottomBar = {
                    BottomNavigationBar(
                        navController, isButton1Enabled, isButton2Enabled,
                        showNotesButton = true, showGPTButton = false
                    )
                },
                content = {}
            )

            MessageList(chatList = chatHistoryManager.chatDataList,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .absoluteOffset(y = 50.dp)
                    .background(Color.DarkGray.copy(0.7f)))
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MessageList(chatList: List<ChatData>, modifier: Modifier) {
    if (chatList.isEmpty()) {
        Text(
            text = "No chat history found.",
            style = MaterialTheme.typography.h6,
            modifier = Modifier
                .padding(16.dp)
        )
    } else {
        LazyColumn(
            modifier
        ) {
            items(chatList.size) { index ->
                ListItem() {
                    val chatData = chatList[index]
                    Text(
                        text = chatData.chatHistoryName,
                        color = Color.Cyan,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable(
                                onClick = {

                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun Chat() {
    Text(
        text = "Howdy",
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun DrawerContent(user: List<User>,
                  gradientColors: List<Color> = listOf(Color(0xFFF70A74),
                      Color(0xFFF59118)), onCloseDrawer: () -> Unit) {
    val helper = Helper()
    val (newUsername, setNewUsername) = rememberSaveable { mutableStateOf(user[0].username) }
    val (newApiKey, setNewApiKey) = rememberSaveable { mutableStateOf(user[0].apiKey) }
    val (currentPassword, setCurrentPassword) = rememberSaveable { mutableStateOf("") }
    val (newPassword, setNewPassword) = rememberSaveable { mutableStateOf("") }
    val (validateNewPassword, setValidateNewPassword) = rememberSaveable { mutableStateOf("") }
    var currentPasswordHidden by rememberSaveable { mutableStateOf(true) }
    var newPasswordHidden by rememberSaveable { mutableStateOf(true) }
    var validatePasswordHidden by rememberSaveable { mutableStateOf(true) }
    var apiKeyHidden by rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .background(Color.DarkGray)
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = gradientColors)),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 36.dp)
    ) {
        item {
            Text(text = "Profile", modifier = Modifier.padding(bottom = 16.dp))
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
                            if (apiKeyHidden) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff
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
                if (currentPasswordHidden) PasswordVisualTransformation()
                else VisualTransformation.None,
                trailingIcon = {
                    IconButton(onClick = { currentPasswordHidden = !currentPasswordHidden }) {
                        val visibilityIcon =
                            if (currentPasswordHidden) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff
                        // Please provide localized description for accessibility services
                        val description = if (currentPasswordHidden) "Show password"
                        else "Hide password"
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
                if (newPasswordHidden) PasswordVisualTransformation()
                else VisualTransformation.None,
                trailingIcon = {
                    IconButton(onClick = { newPasswordHidden = !newPasswordHidden }) {
                        val visibilityIcon =
                            if (newPasswordHidden) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff
                        // Please provide localized description for accessibility services
                        val description = if (newPasswordHidden) "Show password"
                        else "Hide password"
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
                if (validatePasswordHidden) PasswordVisualTransformation()
                else VisualTransformation.None,
                trailingIcon = {
                    IconButton(onClick = { validatePasswordHidden = !validatePasswordHidden }) {
                        val visibilityIcon =
                            if (validatePasswordHidden) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff
                        // Please provide localized description for accessibility services
                        val description = if (validatePasswordHidden) "Show password"
                        else "Hide password"
                        Icon(imageVector = visibilityIcon, contentDescription = description)
                    }
                }
            )
            Button(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Cyan,
                    contentColor = Color.Black
                ),
                onClick = {
                    // TODO: Validate password and update user data (username, API key, password)

                }
            ) {
                Text("Update User Info")
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, isButton1Enabled: Boolean,
                        isButton2Enabled: Boolean, showNotesButton: Boolean, showGPTButton: Boolean)
{
    BottomAppBar(
        backgroundColor = Color.Transparent,
        content = {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (showNotesButton) {
                Button(
                    modifier = Modifier
                        .size(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = Color.Cyan,
                        disabledBackgroundColor = Color.Transparent,
                        disabledContentColor = Color.Transparent
                    ),
                    onClick = {
                        navController.navigate("Notes")
                    },
                    enabled = isButton1Enabled
                ) {
                    Icon(
                        modifier = Modifier
                            .size(80.dp),
                        imageVector = Icons.Default.Note,
                        contentDescription = "Open Notes"
                    )
                }
                }
                if (showGPTButton) {
                Button(
                    modifier = Modifier
                        .size(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = Color.Cyan,
                        disabledBackgroundColor = Color.Transparent,
                        disabledContentColor = Color.Transparent
                    ),
                    onClick = {
                        navController.navigate("GPT")
                    },
                    enabled = isButton2Enabled
                ) {
                    Icon(
                        modifier = Modifier
                            .size(80.dp),
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Open GPT"
                    )
                }
                    Button(
                        modifier = Modifier
                            .size(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        onClick = {}) {
                        Icon(
                            modifier = Modifier
                                .size(80.dp),
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "Add Note"
                        )

                    }
                }
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun FeaturePlayground(navController: NavHostController, userManager: UserManager,
        isButton1Enabled: Boolean, isButton2Enabled: Boolean, chatHistoryManager: ChatHistoryManager,
        models: Models, onNotes: () -> Unit) {
    val context = LocalContext.current
    val helper = Helper()
    val isRecording = remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val user = userManager.loadUsers()
    val coroutineScope = rememberCoroutineScope()
    val transcription = remember { mutableStateOf("") }

    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(user, onCloseDrawer = {
                coroutineScope.launch {
                    delay(timeMillis = 250)
                    drawerState.close()
                }
            })
        },
        modifier = Modifier.zIndex(1f) // Set a higher zIndex for the ModalDrawer
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Scaffold(
                backgroundColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        backgroundColor = Color.Transparent,
                        contentColor = Color.White,
                        title = { Text(text = "FeaturePlayground") },
                        navigationIcon = {
                            IconButton(
                                onClick = {
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
                                    Icons.Filled.Info,
                                    contentDescription = "App informations",
                                    tint = Color.White
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {},
                        modifier = Modifier
                            .padding(32.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        val tempAudioFile = withContext(Dispatchers.IO) {
                                            File.createTempFile("temp_audio", ".mp3",
                                                context.cacheDir)
                                        }
                                        isRecording.value = true
                                        coroutineScope.launch {
                                            val result = models.whisper(isRecording,
                                                tempAudioFile = tempAudioFile)
                                            transcription.value = result
                                        }
                                        tryAwaitRelease()
                                        isRecording.value = false
                                    }
                                )
                            },
                        content = {
                            Icon(Icons.Filled.Mic, "")
                        }
                    )
                },
                floatingActionButtonPosition = FabPosition.End,
                isFloatingActionButtonDocked = true,
                bottomBar = {
                    BottomNavigationBar(
                        navController, isButton1Enabled, isButton2Enabled,
                        showNotesButton = true, showGPTButton = false
                    )
                },
                content = {}
            )

            Column(modifier = Modifier
                .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                helper.unsafeTextField(
                    value =transcription.value,
                    onValueChange = { transcription.value = it },
                    label = "Transcription",
                    readOnly = true
                )
            }
        }
    }
}
