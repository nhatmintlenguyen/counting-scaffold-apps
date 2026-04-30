package com.example.dadn_app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.dadn_app.core.utils.TokenManager
import com.example.dadn_app.ui.theme.Background
import com.example.dadn_app.ui.theme.Dadn_appTheme
import com.example.dadn_app.ui.theme.OnSurface
import com.example.dadn_app.ui.theme.OnSurfaceVariant
import com.example.dadn_app.ui.theme.OutlineVariant
import com.example.dadn_app.ui.theme.Primary
import com.example.dadn_app.ui.theme.Error
import com.example.dadn_app.ui.theme.SurfaceContainerLowest

private fun defaultProfileEmail(): String {
    return try {
        if (TokenManager.isLoggedIn) "user@example.com" else "user@example.com"
    } catch (_: UninitializedPropertyAccessException) {
        "user@example.com"
    }
}

private fun defaultProfileUsername(email: String): String {
    return email.substringBefore("@")
}

@Composable
fun SettingsScreen(
    navController: NavHostController? = null,
    onLogout: () -> Unit = {},
    onAvatarChanged: (String?) -> Unit = {},
) {
    val context = LocalContext.current
    val profilePrefs = remember {
        context.getSharedPreferences(ProfileStore.PREFS, Context.MODE_PRIVATE)
    }

    val fallbackEmail = remember { defaultProfileEmail() }
    val fallbackUsername = remember(fallbackEmail) { defaultProfileUsername(fallbackEmail) }

    var avatarUri by remember {
        mutableStateOf(profilePrefs.getString(ProfileStore.KEY_AVATAR_URI, null)?.toUri())
    }
    var username by remember {
        mutableStateOf(profilePrefs.getString(ProfileStore.KEY_USERNAME, fallbackUsername) ?: fallbackUsername)
    }
    var email by remember {
        mutableStateOf(profilePrefs.getString(ProfileStore.KEY_EMAIL, fallbackEmail) ?: fallbackEmail)
    }
    var showPasswordEditor by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    fun persistProfile(nextUsername: String = username, nextEmail: String = email) {
        profilePrefs.edit()
            .putString(ProfileStore.KEY_USERNAME, nextUsername)
            .putString(ProfileStore.KEY_EMAIL, nextEmail)
            .apply()
    }

    val avatarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        val savedUri = ProfileStore.saveAvatarToInternal(context, uri)
        if (savedUri != null) {
            avatarUri = savedUri
            profilePrefs.edit().putString(ProfileStore.KEY_AVATAR_URI, savedUri.toString()).apply()
            onAvatarChanged(savedUri.toString())
        } else {
            Toast.makeText(context, "Unable to save avatar", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navController?.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Primary,
                )
            }
            Text(
                text = "Settings",
                color = Primary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Filled.Save, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
        }

        Spacer(Modifier.height(56.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(142.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE6ECF7)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (avatarUri != null) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "Profile avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(72.dp),
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            showPasswordEditor = true
                            avatarLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = Primary,
                    border = BorderStroke(3.dp, Background),
                    shadowElevation = 4.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit avatar and password",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            Text(
                text = username.ifBlank { "User" },
                color = OnSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = email.ifBlank { "No email" },
                color = OnSurfaceVariant,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(44.dp))

        Text(
            text = "PERSONAL INFORMATION",
            color = OnSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
        )

        Spacer(Modifier.height(16.dp))

        ProfileInfoField(
            label = "USER NAME",
            value = username,
            icon = Icons.Filled.Person,
            onValueChange = {
                username = it
                persistProfile(nextUsername = it)
            },
        )

        Spacer(Modifier.height(16.dp))

        ProfileInfoField(
            label = "EMAIL ADDRESS",
            value = email,
            icon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email,
            onValueChange = {
                email = it
                persistProfile(nextEmail = it)
            },
        )

        if (showPasswordEditor) {
            Spacer(Modifier.height(28.dp))
            Text(
                text = "PASSWORD",
                color = OnSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
            )
            Spacer(Modifier.height(16.dp))

            PasswordField("CURRENT PASSWORD", currentPassword) { currentPassword = it }
            Spacer(Modifier.height(12.dp))
            PasswordField("NEW PASSWORD", newPassword) { newPassword = it }
            Spacer(Modifier.height(12.dp))
            PasswordField("CONFIRM NEW PASSWORD", confirmPassword) { confirmPassword = it }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val storedPassword = profilePrefs.getString(ProfileStore.KEY_PASSWORD, "") ?: ""
                    when {
                        currentPassword.isBlank() -> {
                            Toast.makeText(context, "Please enter your current password", Toast.LENGTH_SHORT).show()
                        }
                        storedPassword.isNotBlank() && currentPassword != storedPassword -> {
                            Toast.makeText(context, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                        }
                        newPassword.length < 6 -> {
                            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                        }
                        newPassword != confirmPassword -> {
                            Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            profilePrefs.edit().putString(ProfileStore.KEY_PASSWORD, newPassword).apply()
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            showPasswordEditor = false
                            Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("Save Password", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(52.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Error),
        ) {
            Text(
                text = "SIGN OUT",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 1.2.sp,
            )
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun ProfileInfoField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F4FC),
        border = BorderStroke(1.dp, Color(0xFFE8EEF8)),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            label = {
                Text(label, color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Primary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = profileTextFieldColors(),
        )
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F4FC),
        border = BorderStroke(1.dp, Color(0xFFE8EEF8)),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            label = {
                Text(label, color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Primary) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = profileTextFieldColors(),
        )
    }
}

@Composable
private fun profileTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    cursorColor = Primary,
    focusedTextColor = OnSurface,
    unfocusedTextColor = OnSurface,
)

@Composable
fun SecurityScreen(onBack: () -> Unit) {
    SimpleSettingsSubScreen(title = "Security & Password", onBack = onBack)
}

@Composable
fun MFAScreen(onBack: () -> Unit) {
    SimpleSettingsSubScreen(title = "Multi-factor Authentication", onBack = onBack)
}

@Composable
fun DevicesScreen(onBack: () -> Unit) {
    SimpleSettingsSubScreen(title = "Authorized Devices", onBack = onBack)
}

@Composable
private fun SimpleSettingsSubScreen(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface)
            }
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        }
        Spacer(Modifier.height(24.dp))
        Text("This section has been removed from the simplified settings page.", color = OnSurfaceVariant)
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun SettingsScreenPreview() {
    val navController = rememberNavController()
    Dadn_appTheme {
        SettingsScreen(navController = navController)
    }
}
