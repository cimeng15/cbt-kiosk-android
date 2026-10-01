package com.sekolah.kioskcbt.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekolah.kioskcbt.R
import com.sekolah.kioskcbt.data.ConfigStore
import com.sekolah.kioskcbt.ui.theme.skadaAccents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MAX_ATTEMPTS = 3

/**
 * Dialog fullscreen untuk membuka kunci kiosk.
 *
 * Aturan:
 *  - Maksimum [MAX_ATTEMPTS] percobaan, setelah itu `lockedOut = true`.
 *  - Password dibandingkan dengan `KioskConfig.exitPassword`.
 *  - Jika config kedaluwarsa (`isExpired`), tetap boleh keluar.
 *
 * ── Penyesuaian tema ──
 * Sebelumnya layar ini memakai `background.copy(alpha = 0.95f)` sehingga di
 * mode gelap berubah menjadi bidang navy gelap rata tanpa struktur. Sekarang
 * memakai pola modal yang benar: scrim + kartu, dengan glow lembut di belakang
 * supaya tetap terlihat premium di light maupun dark mode.
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

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Scrim: meredupkan halaman ujian di belakang ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.skadaAccents.scrim),
        )

        // ── Glow lembut agar tidak terasa datar ──
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(320.dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        // ── Kartu dialog ──
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.skadaAccents.card,
            border = BorderStroke(1.dp, MaterialTheme.skadaAccents.hairline),
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Ikon kunci dalam lingkaran beraksen
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.enter_password),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.unlock_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(22.dp))

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
                    shape = RoundedCornerShape(14.dp),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.unlock_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { tryUnlock() }),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff
                                else Icons.Filled.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )

                // ── Pesan error / sisa percobaan ──
                if (error != null || (attempts > 0 && !lockedOut)) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = when {
                                lockedOut -> stringResource(R.string.locked_out)
                                error != null && attempts > 0 ->
                                    "$error · " + stringResource(
                                        R.string.attempts_remaining,
                                        MAX_ATTEMPTS - attempts,
                                    )
                                error != null -> error.orEmpty()
                                else -> stringResource(
                                    R.string.attempts_remaining,
                                    MAX_ATTEMPTS - attempts,
                                )
                            },
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !lockedOut && !isChecking && password.isNotBlank(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.skadaAccents.disabledTrack,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    onClick = { tryUnlock() },
                ) {
                    Text(
                        text = stringResource(R.string.unlock_button),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }

                Spacer(Modifier.height(6.dp))

                TextButton(onClick = onCancel) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
