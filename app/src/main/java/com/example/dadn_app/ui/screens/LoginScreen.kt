package com.example.dadn_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dadn_app.ui.theme.*
import com.example.dadn_app.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // ── Field state ──────────────────────────────────────────────────────────
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // ── Validation errors (shown inline under the field) ─────────────────────
    var emailError    by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // ── Side effects ──────────────────────────────────────────────────────────
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess()
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    // ── Validation logic ─────────────────────────────────────────────────────
    fun validate(): Boolean {
        var valid = true
        emailError = when {
            email.isBlank()                      -> "Email is required".also { valid = false }
            !android.util.Patterns.EMAIL_ADDRESS
                .matcher(email).matches()         -> "Enter a valid email".also { valid = false }
            else                                  -> null
        }
        passwordError = when {
            password.isBlank() -> "Password is required".also { valid = false }
            password.length < 6 -> "At least 6 characters".also { valid = false }
            else                -> null
        }
        return valid
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData     = data,
                    containerColor   = Error,
                    contentColor     = OnError,
                    shape            = RoundedCornerShape(10.dp),
                )
            }
        },
        containerColor = Background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding),
        ) {
            // ── Top hero banner ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Brush.linearGradient(listOf(Primary, PrimaryContainer))),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Architecture,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "S.C.P",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 4.sp,
                    )
                    Text(
                        text = "Precision Scaffold Counting",
                        fontSize = 13.sp,
                        color = PrimaryFixed.copy(alpha = 0.85f),
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 0.5.sp,
                    )
                }
            }

            // ── Form card ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Welcome back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = "Sign in to your account",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                )

                Spacer(Modifier.height(4.dp))

                // Email
                AuthTextField(
                    value         = email,
                    onValueChange = { email = it; emailError = null },
                    label         = "Email",
                    leadingIcon   = Icons.Filled.Email,
                    errorMessage  = emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                )

                // Password
                AuthTextField(
                    value         = password,
                    onValueChange = { password = it; passwordError = null },
                    label         = "Password",
                    leadingIcon   = Icons.Filled.Lock,
                    errorMessage  = passwordError,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (validate()) vm.login(email, password)
                        }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = OnSurfaceVariant,
                            )
                        }
                    },
                )

                Spacer(Modifier.height(4.dp))

                // Sign in button
                Button(
                    onClick = { if (validate()) vm.login(email, password) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor   = OnPrimary,
                    ),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color     = OnPrimary,
                            modifier  = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        )
                    }
                }

                // Register link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Don't have an account?", fontSize = 13.sp, color = OnSurfaceVariant)
                    TextButton(onClick = onNavigateToRegister) {
                        Text(
                            text = "Register",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    Dadn_appTheme {
        LoginScreen(onNavigateToRegister = {}, onLoginSuccess = {})
    }
}
