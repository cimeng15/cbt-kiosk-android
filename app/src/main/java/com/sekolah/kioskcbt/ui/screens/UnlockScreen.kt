package com.sekolah.kioskcbt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekolah.kioskcbt.R
import com.sekolah.kioskcbt.data.ConfigStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MAX_ATTEMPTS = 3

/**
 * Dialog fullscreen untuk membuka kunci kiosk.
 *
 * Direkonstruksi dari `UnlockScreenKt.UnlockScreen` (UnlockScreen.kt:35).
 *
 * Aturan (dari bytecode):
 *  - Maksimum [MAX_ATTEMPTS] percobaan, setelah itu `lockedOut = true`.
 *  - Password dibandingkan dengan `KioskConfig.exitPassword`.
 *  - Jika config kedaluwarsa (`isExpired`), tetap boleh keluar.
 */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun UnlockScreen(
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var attempts by remember { mutableIntStateOf(0) }
    var lockedOut by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    fun tryUnlock() {
        if (lockedOut || isChecking) return
        isChecking = true
        keyboard?.hide()
        scope.launch {
            val config = withContext(Dispatchers.IO) { ConfigStore.getConfig(context) }
            val expired = config?.isExpired() ?: true
            val correct = config != null && password == config.exitPassword
            isChecking = false
            if (correct || expired) {
                onSuccess()
            } else {
                val newAttempts = attempts + 1
                attempts = newAttempts
                error = "Password salah"
                if (newAttempts >= MAX_ATTEMPTS) lockedOut = true
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.enter_password),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    if (!lockedOut) {
                        password = it
                        error = null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                enabled = !lockedOut && !isChecking,
                isError = error != null,
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { tryUnlock() }),
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "Hide" else "Show")
                    }
                },
            )
            Spacer(Modifier.height(8.dp))

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                )
            }
            if (attempts > 0 && !lockedOut) {
                Text(
                    text = stringResource(R.string.attempts_remaining, MAX_ATTEMPTS - attempts),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                )
            }
            if (lockedOut) {
                Text(
                    text = stringResource(R.string.locked_out),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !lockedOut && !isChecking && password.isNotBlank(),
                onClick = { tryUnlock() },
            ) {
                Text("Buka Kunci", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}
