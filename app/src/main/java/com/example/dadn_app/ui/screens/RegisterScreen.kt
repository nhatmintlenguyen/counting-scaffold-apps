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
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
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
fun RegisterFormCard(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    fullNameError: String?,
    emailError: String?,
    passwordError: String?,
    confirmPasswordError: String?,
    passwordVisible: Boolean,
    confirmVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmVisibility: () -> Unit,
    isLoading: Boolean,
    onRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Create Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface,
                letterSpacing = (-0.4).sp,
            )

            Text(
                text = "Create your account to access the console securely.",
                fontSize = 13.sp,
                color = OnSurfaceVariant,
                lineHeight = 18.sp,
            )

            Spacer(modifier = Modifier.height(2.dp))

            AuthTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = "Full Name",
                leadingIcon = Icons.Filled.Person,
                errorMessage = fullNameError,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { }
                ),
            )

            AuthTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Email Address",
                leadingIcon = Icons.Filled.Email,
                errorMessage = emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { }
                ),
            )

            AuthTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Password",
                leadingIcon = Icons.Filled.Lock,
                errorMessage = passwordError,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { }
                ),
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.VisibilityOff
                            else
                                Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            },
                            tint = OnSurfaceVariant,
                        )
                    }
                },
            )

            AuthTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Confirm Password",
                leadingIcon = Icons.Filled.LockOpen,
                errorMessage = confirmPasswordError,
                visualTransformation = if (confirmVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onRegister() }
                ),
                trailingIcon = {
                    IconButton(onClick = onToggleConfirmVisibility) {
                        Icon(
                            imageVector = if (confirmVisible)
                                Icons.Filled.VisibilityOff
                            else
                                Icons.Filled.Visibility,
                            contentDescription = if (confirmVisible) {
                                "Hide confirm password"
                            } else {
                                "Show confirm password"
                            },
                            tint = OnSurfaceVariant,
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(2.dp))

            Button(
                onClick = onRegister,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary,
                ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = OnPrimary,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "CREATE ACCOUNT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 4.dp),
                color = Color(0xFFE9EDF3)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Already have an account?",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )

                TextButton(
                    onClick = onNavigateToLogin,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess(email)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    fun validate(): Boolean {
        var valid = true

        fullNameError = when {
            fullName.isBlank() -> "Full name is required".also { valid = false }
            fullName.trim().length < 2 -> "Enter a valid name".also { valid = false }
            else -> null
        }

        emailError = when {
            email.isBlank() -> "Email is required".also { valid = false }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Enter a valid email".also { valid = false }
            else -> null
        }

        passwordError = when {
            password.isBlank() -> "Password is required".also { valid = false }
            password.length < 6 -> "At least 6 characters".also { valid = false }
            else -> null
        }

        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Please confirm your password".also { valid = false }
            confirmPassword != password -> "Passwords do not match".also { valid = false }
            else -> null
        }

        return valid
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Error,
                    contentColor = OnError,
                    shape = RoundedCornerShape(12.dp),
                )
            }
        },
        containerColor = Color(0xFFF5F7FB),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF1F5FA),
                            Color(0xFFF7F8FC),
                            Color(0xFFF3F4F8)
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 22.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Primary.copy(alpha = 0.10f)
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Architecture,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Text(
                        text = "ScaffoldCounter Pro",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface
                    )

                    Text(
                        text = "Create your account securely",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant
                    )
                }

                RegisterFormCard(
                    fullName = fullName,
                    onFullNameChange = {
                        fullName = it
                        fullNameError = null
                    },
                    email = email,
                    onEmailChange = {
                        email = it
                        emailError = null
                    },
                    password = password,
                    onPasswordChange = {
                        password = it
                        passwordError = null
                    },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = {
                        confirmPassword = it
                        confirmPasswordError = null
                    },
                    fullNameError = fullNameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    passwordVisible = passwordVisible,
                    confirmVisible = confirmVisible,
                    onTogglePasswordVisibility = {
                        passwordVisible = !passwordVisible
                    },
                    onToggleConfirmVisibility = {
                        confirmVisible = !confirmVisible
                    },
                    isLoading = uiState.isLoading,
                    onRegister = {
                        focusManager.clearFocus()
                        if (validate()) vm.register(fullName, email, password)
                    },
                    onNavigateToLogin = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 360.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "By continuing, you agree to our Terms and Privacy Policy.",
                    fontSize = 11.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    Dadn_appTheme {
        RegisterScreen(
            onNavigateToLogin = {},
            onRegisterSuccess = {}
        )
    }
}