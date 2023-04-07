package com.example.voiceassistant

import android.inputmethodservice.Keyboard
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voiceassistant.ui.theme.VoiceAssistantTheme
import java.security.Key

class Helper() {
    @Composable
    fun unsafeTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        isError: Boolean = false,
        errorMessage: String? = null,
        modifier: Modifier = Modifier,
        trailingIcon: @Composable (() -> Unit)? = null
    ) {
        Column(modifier = modifier) {
            OutlinedTextField(
                value = value,
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
                onValueChange = onValueChange,
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text(label) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = isError,
                trailingIcon = trailingIcon,
            )

            if (isError && !errorMessage.isNullOrEmpty()) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }

    @Composable
    fun safeTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        visualTransformation: VisualTransformation = VisualTransformation.None,
        isError: Boolean = false,
        errorMessage: String? = null,
        modifier: Modifier = Modifier,
        trailingIcon: @Composable (() -> Unit)? = null
    ) {
        Column(modifier = modifier) {
            OutlinedTextField(
                value = value,
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
                onValueChange = onValueChange,
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text(label) },
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = isError,
                trailingIcon = trailingIcon
            )

            if (isError && !errorMessage.isNullOrEmpty()) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}