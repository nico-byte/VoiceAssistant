package com.example.voiceassistant

import android.os.Bundle
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.TextRange
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.gson.Gson

class LogInActivity : ComponentActivity() {
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gson = Gson()
        userManager = UserManager(this, gson)
        setContent { LogIn() }
    }

    @Composable
    fun LogIn() {
        var currentScreen by remember { mutableStateOf("welcome") }

        when (currentScreen) {
            "welcome" -> WelcomeScreen(
                onLoginSelected = { currentScreen = "login" },
                onRegisterSelected = { currentScreen = "register" }
            )
            "register" -> RegistrationScreen(
                userManager = userManager,
                onUserRegistered = { _ ->
                    currentScreen = "login"
                }
            )
            "login" -> LoginScreen(
                userManager = userManager,
                onSuccess = {
                    currentScreen = "choice"
                },
                onFailure = {
                    //TODO: Add Handling for unsuccessful Login
                }
            )
            "choice" -> ChoiceScreen()
        }
    }
}

@Composable
fun WelcomeScreen(onLoginSelected: () -> Unit, onRegisterSelected: () -> Unit) {
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
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
                onClick = onLoginSelected) {
                Text("LOG IN")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
                onClick = onRegisterSelected) {
                Text("REGISTER")
            }
        }
    }
}

@Composable
fun ChoiceScreen() {
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

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
                onClick = {
                    context.startActivity(Intent(context, GPTActivity::class.java))
                }) {
                Text("GPT")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
                onClick = {
                    context.startActivity(Intent(context, NotesActivity::class.java))
                }) {
                Text("Notes")
            }
        }
    }
}

@Composable
fun RegistrationScreen(userManager: UserManager, onUserRegistered: (User) -> Unit) {
    var (username, setUsername) = remember {
        mutableStateOf(TextFieldValue())
    }

    var (password, setPassword) = remember {
        mutableStateOf(TextFieldValue())
    }

    var passwordHidden by rememberSaveable { mutableStateOf(true) }

    var (apiKey, setApiKey) = remember {
        mutableStateOf(TextFieldValue())
    }
    var apiKeyHidden by rememberSaveable { mutableStateOf(true) }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painterResource(id = R.drawable.musky),
            contentDescription = "",
            contentScale = ContentScale.FillBounds, // or some other scale
            modifier = Modifier.matchParentSize()
        )
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = username,
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
                    setUsername(newUsername.ofMaxLength(16))
                },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text("Enter Username") },
                trailingIcon = {
                    Icon(Icons.Default.Clear,
                        contentDescription = "clear text",
                        modifier = Modifier
                            .clickable {
                                setUsername(TextFieldValue(""))
                            }
                    )
                }
            )

            OutlinedTextField(
                value = password,
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
                onValueChange = { newPassword ->
                    setPassword(newPassword.ofMaxLength(32))
                },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text("Enter password") },
                visualTransformation =
                if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordHidden = !passwordHidden }) {
                        val visibilityIcon =
                            if (passwordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        // Please provide localized description for accessibility services
                        val description = if (passwordHidden) "Show password" else "Hide password"
                        Icon(imageVector = visibilityIcon, contentDescription = description)
                    }
                }
            )

            OutlinedTextField(
                value = apiKey,
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
                    setApiKey(newApiKey.ofMaxLength(1024))
                },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text("Enter API Key") },
                visualTransformation =
                if (apiKeyHidden) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { apiKeyHidden = !passwordHidden }) {
                        val visibilityIcon =
                            if (apiKeyHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        // Please provide localized description for accessibility services
                        val description = if (apiKeyHidden) "Show password" else "Hide password"
                        Icon(imageVector = visibilityIcon, contentDescription = description)
                    }
                }
            )
            val uriHandler = LocalUriHandler.current
            val context = LocalContext.current
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
                onClick = {
                uriHandler.openUri("https://platform.openai.com/account/api-keys")
            }, shape = RoundedCornerShape(20.dp)) {
                Text(text = "GET API KEY")
            }

            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
                onClick = {
                val newUser = User(username.text, password.text, apiKey.text)
                userManager.saveUser(newUser)
                onUserRegistered(newUser)
            }, shape = RoundedCornerShape(20.dp)) {
                Text(text = "SIGN IN")
            }
        }
    }
}


@Composable
fun LoginScreen(userManager: UserManager, onSuccess: () -> Unit, onFailure: () -> Unit) {
    var (username, setUsername) = remember {
        mutableStateOf(TextFieldValue())
    }

    var (password, setPassword) = remember {
        mutableStateOf(TextFieldValue())
    }

    var passwordHidden by rememberSaveable { mutableStateOf(true) }

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
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = username,
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
                    setUsername(newUsername.ofMaxLength(16))
                },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text("Enter Username") },
                trailingIcon = {
                    Icon(Icons.Default.Clear,
                        contentDescription = "clear text",
                        modifier = Modifier
                            .clickable {
                                setUsername(TextFieldValue(""))
                            }
                    )
                }
            )

            OutlinedTextField(
                value = password,
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
                onValueChange = { newPassword ->
                    setPassword(newPassword.ofMaxLength(32))
                },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text("Enter password") },
                visualTransformation =
                if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordHidden = !passwordHidden }) {
                        val visibilityIcon =
                            if (passwordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        // Please provide localized description for accessibility services
                        val description = if (passwordHidden) "Show password" else "Hide password"
                        Icon(imageVector = visibilityIcon, contentDescription = description)
                    }
                }
            )

            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
                onClick = {
                val users = userManager.loadUsers()
                val user = users.find { it.username == username.text && it.password == password.text }
                if (user != null) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }, shape = RoundedCornerShape(20.dp)) {
                Text(text = "LOG IN")
            }
        }
    }
}

fun TextFieldValue.ofMaxLength(maxLength: Int): TextFieldValue {
    val overLength = text.length - maxLength
    return if (overLength > 0) {
        val headIndex = selection.end - overLength
        val trailIndex = selection.end
        // Under normal conditions, headIndex >= 0
        if (headIndex >= 0) {
            copy(
                text = text.substring(0, headIndex) + text.substring(trailIndex, text.length),
                selection = TextRange(headIndex)
            )
        } else {
            // exceptional
            copy(text.take(maxLength), selection = TextRange(maxLength))
        }
    } else {
        this
    }
}