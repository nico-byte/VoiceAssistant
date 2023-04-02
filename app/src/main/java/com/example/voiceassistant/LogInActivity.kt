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

class LogInActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager
    private var registeredUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager(this)
        registeredUser = authManager.loadUser()
        setContent { LogIn()
        }
    }

    @Composable
    fun LogIn() {
        var currentScreen by remember { mutableStateOf("welcome") }

        when (currentScreen) {
            "welcome" -> WelcomeScreen(
                onLoginSelected = { currentScreen = "login" },
                onRegisterSelected = { currentScreen = "register" }
            )
            "register" -> RegistrationScreen { username, password, apiKey ->
                val newUser = User(username, password, apiKey) // Corrected variable name
                authManager.saveUser(newUser) // Save the user data
                registeredUser = newUser
                currentScreen = "login"
            }
            "login" -> LoginScreen { username, password ->
                registeredUser?.let {
                    if (it.username == username && it.password == password) {
                        startActivity(Intent(this@LogInActivity, GPTActivity::class.java))
                    } else {
                        // Handle failed login, for example, show an error message
                    }
                }
            }
        }
    }

    @Composable
    fun WelcomeScreen(onLoginSelected: () -> Unit, onRegisterSelected: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onLoginSelected) {
                Text("LOG IN")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRegisterSelected) {
                Text("REGISTER")
            }
        }
    }
}

@Composable
fun RegistrationScreen(onRegister: (String, String, String) -> Unit) {
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
                    focusedBorderColor = Color.Cyan,
                    unfocusedBorderColor = Color.Cyan,
                    cursorColor = Color.Black,
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
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = Color.Black,
                    backgroundColor = Color.White,
                    disabledLabelColor = Color.Transparent,
                    focusedLabelColor = Color.Transparent,
                    unfocusedLabelColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
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
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = Color.Black,
                    backgroundColor = Color.White,
                    disabledLabelColor = Color.Transparent,
                    focusedLabelColor = Color.Transparent,
                    unfocusedLabelColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
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
            Button(onClick = {
                uriHandler.openUri("https://platform.openai.com/account/api-keys")
            }, shape = RoundedCornerShape(20.dp)) {
                Text(text = "GET API KEY")
            }

            Button(onClick = {
                onRegister(username.toString(), password.toString(), apiKey.toString())
            }, shape = RoundedCornerShape(20.dp)) {
                Text(text = "SIGN IN")
            }
        }
    }
}


@Composable
fun LoginScreen(onLogin: (String, String) -> Unit) {
    var (username, setUsername) = remember {
        mutableStateOf(TextFieldValue()) }

    var (password, setPassword) = remember {
        mutableStateOf(TextFieldValue()) }

    var passwordHidden by rememberSaveable { mutableStateOf(true) }

    Box(modifier = Modifier
        .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painterResource(id = R.drawable.musky),
            contentDescription = "",
            contentScale = ContentScale.FillBounds, // or some other scale
            modifier = Modifier.matchParentSize()
        )
        OutlinedTextField(
            value = username,
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color.Black,
                backgroundColor = Color.White,
                disabledLabelColor = Color.Transparent,
                focusedLabelColor = Color.Transparent,
                unfocusedLabelColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
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
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color.Black,
                backgroundColor = Color.White,
                disabledLabelColor = Color.Transparent,
                focusedLabelColor = Color.Transparent,
                unfocusedLabelColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
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

        Button(onClick = {
            onLogin(username.toString(), password.toString())
        }, shape = RoundedCornerShape(20.dp)) {
            Text(text = "LOG IN")
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