package com.example.dadn_app.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TabletMac
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
import com.example.dadn_app.ui.theme.SurfaceContainerHigh
import com.example.dadn_app.ui.theme.SurfaceContainerLow
import com.example.dadn_app.ui.theme.SurfaceContainerLowest
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PROFILE_PREFS = "scp_profile_prefs"
private const val KEY_AVATAR_URI = "avatar_uri"

private fun saveAvatarToInternal(context: Context, uri: Uri): Uri? {
    return try {
        val file = File(context.filesDir, "user_avatar_permanent.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        } ?: return null
        Uri.fromFile(file)
    } catch (_: Exception) {
        null
    }
}

private fun currentSessionLabel(): String {
    return try {
        if (TokenManager.accessToken != null) "Authenticated session" else "user@example.com"
    } catch (_: UninitializedPropertyAccessException) {
        "user@example.com"
    }
}

@Composable
fun SettingsScreen(
    navController: NavHostController? = null,
    onLogout: () -> Unit = {},
) {
    val context = LocalContext.current
    val profilePrefs = remember {
        context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
    }

    var smartFocus by remember { mutableStateOf(true) }
    var offlineCache by remember { mutableStateOf(true) }
    var hapticFeedback by remember { mutableStateOf(false) }
    var avatarUri by remember {
        mutableStateOf(profilePrefs.getString(KEY_AVATAR_URI, null)?.toUri())
    }

    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        val savedUri = saveAvatarToInternal(context, uri)
        if (savedUri != null) {
            avatarUri = savedUri
            profilePrefs.edit().putString(KEY_AVATAR_URI, savedUri.toString()).apply()
        } else {
            Toast.makeText(context, "Unable to save avatar", Toast.LENGTH_SHORT).show()
        }
    }

    val finalEmail = remember { currentSessionLabel() }
    val finalName = remember(finalEmail) {
        if (finalEmail.contains("@")) finalEmail.substringBefore("@") else "S.C.P Operator"
    }
    val lastSyncTime = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
            Text(
                text = "Settings",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Configure your industrial scanning protocols and system parameters.",
                fontSize = 15.sp,
                color = OnSurfaceVariant,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceContainerLowest,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(85.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(OnSurface)
                            .clickable { avatarLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUri != null) {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Profile avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text(
                            text = finalName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text("Profile Account", fontSize = 14.sp, color = OnSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            SettingsSectionHeader("Account", "Manage your secure credentials and session security levels.")
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                SettingsItemPro(
                    icon = Icons.Default.Lock,
                    title = "Security & Password",
                    value = "",
                    onClick = { navController?.navigate(Routes.SECURITY) }
                )
                SettingsItemPro(
                    icon = Icons.Default.Security,
                    title = "Multi-factor Authentication",
                    value = "ENABLED",
                    isBadge = true,
                    onClick = { navController?.navigate(Routes.MFA) }
                )
                SettingsItemPro(
                    icon = Icons.Default.Devices,
                    title = "Authorized Devices",
                    value = "",
                    onClick = { navController?.navigate(Routes.DEVICES) }
                )
            }

            Spacer(Modifier.height(24.dp))

            SettingsSectionHeader("Scan Preferences", "Optimize the scan engine for your hardware environment.")
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                SettingsToggleItem(
                    title = "Smart Focus Assist",
                    subtitle = "AI-driven clarity optimization for industrial captures",
                    checked = smartFocus,
                    onCheckedChange = { smartFocus = it }
                )
                SettingsToggleItem(
                    title = "Offline Cache Mode",
                    subtitle = "Store scanned data when no network is found",
                    checked = offlineCache,
                    onCheckedChange = { offlineCache = it }
                )
                SettingsToggleItem(
                    title = "Haptic Confirmation",
                    subtitle = "Vibrate device on successful scan",
                    checked = hapticFeedback,
                    onCheckedChange = { hapticFeedback = it }
                )
            }

            Spacer(Modifier.height(16.dp))

            SettingsSectionHeader("System Information", "Core engine status and versioning metadata.")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceContainerLowest,
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SystemInfoRow("CORE ENGINE", "v2.4.12-pro")
                    SystemInfoRow("LAST SYNC", lastSyncTime)
                    SystemInfoRow("NETWORK PROTOCOL", "TLS 1.3 Secure", hasDot = true)
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("You are currently logged in as ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = OnSurface)) {
                            append(finalEmail)
                        }
                    },
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC51111))
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(120.dp))
            }
        }
    }
}

@Composable
fun SecurityScreen(onBack: () -> Unit) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        SettingsBackHeader(title = "Security & Password", onBack = onBack)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = oldPass,
            onValueChange = { oldPass = it },
            label = { Text("Current Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = newPass,
            onValueChange = { newPass = it },
            label = { Text("New Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPass,
            onValueChange = { confirmPass = it },
            label = { Text("Confirm New Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                when {
                    oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank() ->
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    newPass != confirmPass ->
                        Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                    newPass.length < 6 ->
                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    else -> {
                        Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Update Password", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MFAScreen(onBack: () -> Unit) {
    var isEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        SettingsBackHeader(title = "Multi-factor Authentication", onBack = onBack)
        Spacer(Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceContainerLowest,
            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.35f))
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Two-Step Verification", fontWeight = FontWeight.Bold, color = OnSurface)
                    Text(
                        "Add an extra layer of security",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                )
            }
        }
    }
}

@Composable
fun DevicesScreen(onBack: () -> Unit) {
    val deviceName = remember {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val model = Build.MODEL.orEmpty()
        if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.replaceFirstChar { it.uppercase() }
        } else {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model".trim()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        SettingsBackHeader(title = "Authorized Devices", onBack = onBack)
        Spacer(Modifier.height(24.dp))

        DeviceRow(name = "$deviceName (This device)", status = "Active now")
    }
}

@Composable
private fun SettingsBackHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface)
        }
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
    }
}

@Composable
private fun DeviceRow(name: String, status: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerLowest
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when {
                    name.contains("PC", ignoreCase = true) -> Icons.Default.Computer
                    name.contains("iPad", ignoreCase = true) -> Icons.Default.TabletMac
                    name.contains("phone", ignoreCase = true) -> Icons.Default.PhoneAndroid
                    else -> Icons.Default.Smartphone
                },
                contentDescription = null,
                tint = Primary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold, color = OnSurface)
                Text(status, fontSize = 12.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, description: String) {
    Column {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Primary)
        Text(
            description,
            fontSize = 14.sp,
            color = OnSurfaceVariant,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerLow,
        onClick = { onCheckedChange(!checked) }
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = Primary)
            )
        }
    }
}

@Composable
private fun SystemInfoRow(label: String, value: String, hasDot: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (hasDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Primary)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(value, fontSize = 13.sp, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsItemPro(
    icon: ImageVector,
    title: String,
    value: String,
    isBadge: Boolean = false,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = OnSurface, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(20.dp))
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                modifier = Modifier.weight(1f)
            )
            if (value.isNotEmpty()) {
                if (isBadge) {
                    Surface(color = SurfaceContainerHigh, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            value,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                } else {
                    Text(value, fontSize = 14.sp, color = OnSurfaceVariant)
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = OutlineVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun SettingsScreenPreview() {
    val navController = rememberNavController()
    Dadn_appTheme {
        SettingsScreen(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
private fun SecurityScreenPreview() {
    Dadn_appTheme {
        SecurityScreen(onBack = {})
    }
}
