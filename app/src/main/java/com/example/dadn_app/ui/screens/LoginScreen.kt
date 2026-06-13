package com.example.dadn_app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dadn_app.R
import com.example.dadn_app.ui.theme.Dadn_appTheme
import com.example.dadn_app.ui.theme.Error
import com.example.dadn_app.ui.theme.OnError
import com.example.dadn_app.ui.theme.OnSurface
import com.example.dadn_app.ui.theme.OnSurfaceVariant
import com.example.dadn_app.ui.theme.Primary
import com.example.dadn_app.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val webClientId = stringResource(R.string.google_web_client_id)

    val googleSignInClient = remember(webClientId) {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(webClientId)
            .build()
        GoogleSignIn.getClient(context, options)
    }

    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data ?: Intent())
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                vm.googleSignInFailed("Google did not return an ID token. Check google_web_client_id.")
            } else {
                vm.signInWithGoogle(idToken, account.email)
            }
        } catch (e: ApiException) {
            vm.googleSignInFailed(googleSignInErrorMessage(e.statusCode))
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            vm.clearSuccess()
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
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
                            Color(0xFFF3F4F8),
                        ),
                    ),
                )
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 28.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Primary.copy(alpha = 0.10f),
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Architecture,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }

                    Text(
                        text = "ScaffoldCounter Pro",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface,
                    )

                    Text(
                        text = "Sign in or create an account with Google",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                    )
                }

                GoogleAuthCard(
                    isLoading = uiState.isLoading,
                    onGoogleSignIn = {
                        if (webClientId.isBlank() || webClientId == "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com") {
                            vm.googleSignInFailed("Missing Google web client ID in strings.xml.")
                        } else {
                            googleSignInClient.signOut().addOnCompleteListener {
                                signInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 360.dp),
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "By continuing, you agree to our Terms and Privacy Policy.",
                    fontSize = 11.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}


private fun googleSignInErrorMessage(statusCode: Int): String {
    return when (statusCode) {
        10 -> "Google sign-in config error (10). Check package name, SHA-1, and web client ID."
        12500 -> "Google sign-in failed (12500). Check OAuth consent screen and Google Play services."
        12501 -> "Google sign-in was cancelled."
        12502 -> "Google sign-in is already in progress."
        else -> "Google sign-in failed: $statusCode"
    }
}

@Composable
private fun GoogleAuthCard(
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Continue securely",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface,
            )

            Text(
                text = "Use your Gmail account. New emails are registered automatically; existing emails are signed in.",
                fontSize = 13.sp,
                color = OnSurfaceVariant,
                lineHeight = 18.sp,
            )

            OutlinedButton(
                onClick = onGoogleSignIn,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFD9E1EC)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = OnSurface,
                    disabledContainerColor = Color(0xFFF4F6FA),
                    disabledContentColor = OnSurfaceVariant,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Primary,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Login,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "SIGN IN / SIGN UP WITH GOOGLE",
                        color = OnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    Dadn_appTheme {
        LoginScreen(onLoginSuccess = {})
    }
}
