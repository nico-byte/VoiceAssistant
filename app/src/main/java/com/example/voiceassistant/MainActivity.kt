package com.example.voiceassistant

import android.os.Bundle
import androidx.compose.ui.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.voiceassistant.ui.theme.VoiceAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {MainActivityComposable()}
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
@Preview(showBackground = true)
@Composable
fun MainActivityComposable() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Helper.getWhiteColor()
    )
    { User() }
}

@Composable
fun User() {
    Column(
        modifier = Modifier
            .wrapContentSize(Alignment.Center),
        verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        UserNameField()
        PasswordField()
        LogInButton()
        CreateUserButton()
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun DefaultPreview() {
    VoiceAssistantTheme {
        Greeting("Android")
    }
}
@Composable
fun UserNameField() {
    var (text, setText) = remember {
        mutableStateOf(TextFieldValue())
    }
    Column( modifier = Modifier
        .absoluteOffset(y = -(50).dp),
        verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        val lightBlue = Color(0xffd8e6ff)
        val blue = Color(0xff76a9ff)
        OutlinedTextField(
            value = text,
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color.Black,
                disabledLabelColor = lightBlue,
                focusedIndicatorColor = Color.Transparent,
            ),
            onValueChange = { newText ->
                setText(newText.ofMaxLength(16))
            },
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            label = { Text("Enter Username") },
            trailingIcon = {
                Icon(Icons.Default.Clear,
                    contentDescription = "clear text",
                    modifier = Modifier
                        .clickable {
                            setText(TextFieldValue(""))
                        }
                )
            }
        )
    }
}

@Composable
fun PasswordField() {
    var (password, setPassword) = remember {
        mutableStateOf(TextFieldValue()) }
    var passwordHidden by rememberSaveable { mutableStateOf(true) }
    Column(
        modifier = Modifier
            .absoluteOffset(y = 50.dp),
        verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        val lightBlue = Color(0xffd8e6ff)

        OutlinedTextField(
            value = password,
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color.Black,
                disabledLabelColor = lightBlue,
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
    }
}

@Composable
fun LogInButton() {
    Column( modifier = Modifier
        .absoluteOffset(y = 90.dp),
        verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Button(onClick = {}, shape = RoundedCornerShape(20.dp)) {
            Text(text = "LOG IN")
        }
    }
}

@Composable
fun CreateUserButton() {
    Column( modifier = Modifier
        .absoluteOffset(y = 120.dp),
        verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Button(onClick = {}, shape = RoundedCornerShape(20.dp)) {
            Text(text = "Create User")
        }
    }
}

object Helper {
    fun getColor(colorString: String) =
        Color(android.graphics.Color.parseColor("#$colorString"))

    fun getGreenColor() = getColor("9963890a")
    fun getWhiteColor() = getColor("FFFFFFFF")
    fun getRedColor() = getColor("99ac162c")
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
