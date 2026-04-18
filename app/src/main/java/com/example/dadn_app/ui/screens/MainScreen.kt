package com.example.dadn_app.ui.screens
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.dadn_app.data.local.database.AppDatabase
import com.example.dadn_app.data.local.entities.ScanEntity
import com.example.dadn_app.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// ─── Data Models ──────────────────────────────────────────────────────────────

data class ScanRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val datetime: String,
    val count: Int = 0,
    val fileType: String,
    val status: String, // "Pending", "Success", "Failed"
    val imageUri: String = ""
)

// ─── Navigation ───────────────────────────────────────────────────────────────

object Routes {
    const val INVENTORY = "inventory"
    const val CURRENT_SCAN = "current_scan"
    const val SETTINGS = "settings"
    const val SECURITY = "security"
    const val MFA = "mfa"
    const val DEVICES = "devices"
}

class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.INVENTORY, "Inventory", Icons.Filled.Inventory, Icons.Filled.Inventory),
    BottomNavItem(Routes.CURRENT_SCAN, "Current Scan", Icons.Filled.QrCodeScanner, Icons.Filled.QrCodeScanner),
    BottomNavItem(Routes.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Filled.Settings)
)

// ─── Helper Functions ─────────────────────────────────────────────────────────

fun nowFormatted(): String = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())

fun triggerHapticFeedback(context: Context) {
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
            val method = vibratorManager.javaClass.getMethod("getDefaultVibrator")
            val vibrator = method.invoke(vibratorManager) as android.os.Vibrator
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(150, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(150, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(150)
            }
        }
    } catch (_: Exception) {
    }
}

fun createCaptureUri(context: Context): Uri {
    val tempFile = File.createTempFile("scaffold_scan_", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
}

fun uriToFileType(context: Context, uri: Uri): String {
    val extension = context.contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "JPG"
    return extension.uppercase()
}

fun saveAvatarToInternal(context: Context, uri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "user_avatar_permanent.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(file)
    } catch (_: Exception) {
        null
    }
}

// ─── Top App Bar ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldCounterTopBar() {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PrecisionManufacturing,
                    contentDescription = "Logo",
                    tint = Color(0xFF007A8A),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "SCAFFOLD COUNTER PRO",
                    color = Color(0xFF102A43),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    )
}

// ─── Bottom Navigation ────────────────────────────────────────────────────────

@Composable
fun ScaffoldCounterBottomNav(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Icon(
                        if (selected) item.iconSelected else item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF007A8A),
                    selectedTextColor = Color(0xFF007A8A),
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8),
                    indicatorColor = Color(0xFF007A8A).copy(alpha = 0.1f)
                )
            )
        }
    }
}

// ─── Main Screen (entry point) ────────────────────────────────────────────────

@Composable
fun MainScreen(
    scans: List<ScanRecord>,
    email: String = "user@example.com",
    onAddScan: (ScanRecord) -> Unit = {},
    onUpdateScan: (ScanRecord) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    var smartFocus by remember { mutableStateOf(true) }
    var offlineCache by remember { mutableStateOf(true) }
    var hapticFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.scanDao().getAllScans().collectLatest { entities ->
            if (scans.isEmpty() && entities.isNotEmpty()) {
                entities.forEach { entity ->
                    onAddScan(ScanRecord(
                        id = entity.id,
                        name = entity.name,
                        datetime = entity.datetime,
                        count = entity.count,
                        fileType = entity.fileType,
                        status = entity.status,
                        imageUri = entity.imageUri
                    ))
                }
            }
        }
    }

    LaunchedEffect(scans.count { it.status == "Pending" }) {
        scans.filter { it.status == "Pending" }.forEach { pending ->
            val processingTime = if (smartFocus) 1200L else 2500L
            kotlinx.coroutines.delay(processingTime)
            
            val updated = pending.copy(status = "Success", count = (12..45).random())
            onUpdateScan(updated)

            if (offlineCache) {
                scope.launch {
                    db.scanDao().insertScan(ScanEntity(
                        id = updated.id,
                        name = updated.name,
                        datetime = updated.datetime,
                        count = updated.count,
                        fileType = updated.fileType,
                        status = updated.status,
                        imageUri = updated.imageUri
                    ))
                }
            }

            if (hapticFeedback) {
                triggerHapticFeedback(context)
            }
        }
    }

    val navController = rememberNavController()
    Scaffold(
        topBar = { ScaffoldCounterTopBar() },
        bottomBar = { ScaffoldCounterBottomNav(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.CURRENT_SCAN,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.INVENTORY) { InventoryScreen(scans) }
            composable(Routes.CURRENT_SCAN) { CurrentScanScreen(scans, onAddScan) }
            composable(Routes.SETTINGS) { 
                SettingsScreen(
                    email = email, 
                    onLogout = onLogout, 
                    navController = navController,
                    smartFocus = smartFocus,
                    onSmartFocusChange = { smartFocus = it },
                    offlineCache = offlineCache,
                    onOfflineCacheChange = { offlineCache = it },
                    hapticFeedback = hapticFeedback,
                    onHapticFeedbackChange = { hapticFeedback = it }
                ) 
            }
            composable(Routes.SECURITY) { SecurityScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.MFA) { MFAScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.DEVICES) { DevicesScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

// ─── Screens ──────────────────────────────────────────────────────────────────

@Composable
fun InventoryScreen(scans: List<ScanRecord>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        Text("Scan History", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF102A43))
        Spacer(Modifier.height(16.dp))
        if (scans.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No scans yet", color = Color(0xFF627D98))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(scans) { scan ->
                    RecentScanCard(scan)
                }
            }
        }
    }
}

@Composable
fun RecentScanCard(scan: ScanRecord) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, null, tint = Color(0xFF94A3B8))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(scan.name, fontWeight = FontWeight.Bold, color = Color(0xFF102A43))
                Text(scan.datetime, fontSize = 12.sp, color = Color(0xFF627D98))
            }
            Text("${scan.count}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF007A8A))
        }
    }
}

@Composable
fun CurrentScanScreen(scans: List<ScanRecord> = emptyList(), onAddScan: (ScanRecord) -> Unit = {}) {
    val context = LocalContext.current
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val lastScan = scans.firstOrNull()
    val isProcessing = lastScan?.status == "Pending"
    val lastActivityTitle = if (lastScan != null) "${lastScan.datetime} • ${lastScan.name}" else "No recent activity"
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCaptureUri?.let { onAddScan(ScanRecord(name="Camera Scan", datetime=nowFormatted(), fileType="JPG", status="Pending", imageUri=it.toString())) }
    }

    val filesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            val selectedUris = if (uris.size > 10) uris.take(10) else uris
            selectedUris.forEach { uri ->
                onAddScan(ScanRecord(
                    name = "File Import",
                    datetime = nowFormatted(),
                    fileType = uriToFileType(context, uri),
                    status = "Pending",
                    imageUri = uri.toString()
                ))
            }
            if (uris.size > 10) Toast.makeText(context, "Only first 10 images selected", Toast.LENGTH_SHORT).show()
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) { pendingCaptureUri = createCaptureUri(context); cameraLauncher.launch(pendingCaptureUri!!) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = if (isProcessing) listOf(Color(0xFFE0F2F1), Background) else listOf(Color(0xFFE1F5FE), Background),
                        radius = 600f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                Surface(
                    modifier = Modifier.size(200.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isProcessing && lastScan.imageUri.isNotEmpty()) {
                            AsyncImage(
                                model = lastScan.imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.CropFree,
                                contentDescription = null,
                                tint = Color(0xFFB0BEC5),
                                modifier = Modifier.size(100.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(3) { Box(modifier = Modifier.size(6.dp, 40.dp).background(Color(0xFFCFD8DC))) }
                            }
                            Box(
                                modifier = Modifier
                                    .size(90.dp, 3.dp)
                                    .rotate(45f)
                                    .background(Color(0xFFE57373).copy(alpha = 0.8f))
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.offset(y = 15.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isProcessing) Color(0xFFE0F2F1) else Color(0xFFE8EAF6),
                    border = BorderStroke(1.dp, if (isProcessing) Color(0xFF007A8A) else Color(0xFFC5CAE9))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isProcessing) {
                            Text("SCANNING...", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF007A8A), letterSpacing = 0.8.sp)
                        } else {
                            Icon(Icons.Default.Bolt, null, tint = Color(0xFF3F51B5), modifier = Modifier.size(16.dp))
                            Text("SYSTEM IDLE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF283593), letterSpacing = 0.8.sp)
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 32.dp)) {
            Text(
                text = if (isProcessing) "Analyzing your\nscaffold image" else "No image is being\nprocessed",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF102A43),
                textAlign = TextAlign.Center,
                lineHeight = 34.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (isProcessing) "Please wait while our AI engine identifies and counts the components in your technical capture." 
                       else "Start a new scaffold scan by capturing an image with your device or browse your local files for a technical blueprint analysis.",
                fontSize = 14.sp,
                color = Color(0xFF627D98),
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = {
                    val isEmulator = android.os.Build.FINGERPRINT.contains("generic") || 
                                   android.os.Build.MODEL.contains("Emulator")
                    
                    if (isEmulator) {
                        onAddScan(ScanRecord(
                            name = "Emulator Scan",
                            datetime = nowFormatted(),
                            fileType = "JPG",
                            status = "Success",
                            count = (10..50).random(),
                            imageUri = ""
                        ))
                        Toast.makeText(context, "Emulator detected: Adding mock result", Toast.LENGTH_SHORT).show()
                    } else {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            pendingCaptureUri = createCaptureUri(context); cameraLauncher.launch(pendingCaptureUri!!)
                        } else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A8A))
            ) {
                Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Start New Scan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(14.dp))
            Surface(
                onClick = { filesLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, null, tint = Color(0xFF007A8A), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Browse Files", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF007A8A))
                }
            }

            Spacer(Modifier.height(48.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(3.dp, 36.dp).clip(CircleShape).background(if (lastScan != null) Color(0xFF007A8A) else Color(0xFFCBD5E1)))
                        Spacer(Modifier.width(20.dp))
                        Column {
                            Text("LAST ACTIVITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                            Text(lastActivityTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334E68))
                        }
                    }
                    Column {
                        Text("ACTIVE MODEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF4FC3F7)))
                            Text("Scaffold-Net v4.2.1", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334E68))
                        }
                    }
                    Column {
                        Text("DEVICE STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                        Text("Precision Ready", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334E68))
                    }
                }
            }
            Spacer(Modifier.height(160.dp))
        }
    }
}

@Composable
fun SettingsScreen(
    email: String? = null, 
    onLogout: () -> Unit = {}, 
    navController: NavHostController? = null,
    smartFocus: Boolean = true,
    onSmartFocusChange: (Boolean) -> Unit = {},
    offlineCache: Boolean = true,
    onOfflineCacheChange: (Boolean) -> Unit = {},
    hapticFeedback: Boolean = false,
    onHapticFeedbackChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val showSoon = { Toast.makeText(context, "Feature coming soon in Pro version", Toast.LENGTH_SHORT).show() }

    var avatarUri by remember { 
        mutableStateOf(com.example.dadn_app.core.utils.TokenManager.userAvatar?.toUri())
    }
    
    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val savedUri = saveAvatarToInternal(context, uri)
            if (savedUri != null) {
                avatarUri = savedUri
                com.example.dadn_app.core.utils.TokenManager.saveAvatar(savedUri.toString())
            }
        }
    }

    val finalEmail = remember(email) {
        com.example.dadn_app.core.utils.TokenManager.userEmail ?: email ?: "user@example.com"
    }
    
    val finalName = remember {
        val nameFromToken = com.example.dadn_app.core.utils.TokenManager.userName
        if (!nameFromToken.isNullOrEmpty() && nameFromToken != "Test Engineer") {
            nameFromToken
        } else {
            finalEmail.split("@").first()
        }
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
                color = Color(0xFF102A43),
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Configure your industrial scanning protocols and system parameters.",
                fontSize = 15.sp,
                color = Color(0xFF627D98),
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
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
                            .background(Color(0xFF102A43))
                            .clickable { avatarLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUri != null) {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text(
                            text = finalName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF102A43)
                        )
                        Text("Profile Account", fontSize = 14.sp, color = Color(0xFF627D98))
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            SettingsSectionHeader("Account", "Manage your secure credentials and session security levels.")
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                SettingsItemPro(Icons.Default.Lock, "Security & Password", "", onClick = { navController?.navigate(Routes.SECURITY) })
                SettingsItemPro(Icons.Default.Shield, "Multi-factor Authentication", "ENABLED", isBadge = true, onClick = { navController?.navigate(Routes.MFA) })
                SettingsItemPro(Icons.Default.Devices, "Authorized Devices", "", onClick = { navController?.navigate(Routes.DEVICES) })
            }

            Spacer(Modifier.height(24.dp))

            SettingsSectionHeader("Scan Preferences", "Optimize the Kinetic Scan engine for your specific hardware environment.")
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                SettingsToggleItem("Smart Focus Assist", "AI-driven clarity optimization for industrial captures", smartFocus) { onSmartFocusChange(it) }
                SettingsToggleItem("Offline Cache Mode", "Store scanned data when no network is found", offlineCache) { onOfflineCacheChange(it) }
                SettingsToggleItem("Haptic Confirmation", "Vibrate device on successful scan", hapticFeedback) { onHapticFeedbackChange(it) }
            }

            Spacer(Modifier.height(16.dp))

            SettingsSectionHeader("System Information", "Core engine status and versioning metadata.")
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFF0F4F8)),
                onClick = { showSoon() }
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF102A43))) {
                            append(finalEmail)
                        }
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF627D98),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
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
        modifier = Modifier.fillMaxSize().background(Background).padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Security & Password", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))
        
        OutlinedTextField(
            value = oldPass, onValueChange = { oldPass = it },
            label = { Text("Current Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = newPass, onValueChange = { newPass = it },
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPass, onValueChange = { confirmPass = it },
            label = { Text("Confirm New Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { 
                if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else if (newPass != confirmPass) {
                    Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                } else if (newPass.length < 6) {
                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A8A))
        ) {
            Text("Update Password", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MFAScreen(onBack: () -> Unit) {
    var isEnabled by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Multi-factor Authentication", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Two-Step Verification", fontWeight = FontWeight.Bold)
                    Text("Add an extra layer of security", fontSize = 13.sp, color = Color(0xFF627D98))
                }
                Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
            }
        }
    }
}

@Composable
fun DevicesScreen(onBack: () -> Unit) {
    val deviceName = remember {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        if (model.startsWith(manufacturer, ignoreCase = true)) model.replaceFirstChar { it.uppercase() }
        else "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
    }

    val devices = listOf(
        "$deviceName (This device)" to "Active now"
    )
    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Authorized Devices", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
        
        devices.forEach { (name, status) ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (name.contains("PC")) Icons.Default.Computer else if (name.contains("iPad")) Icons.Default.TabletMac else Icons.Default.Smartphone,
                        null, 
                        tint = Color(0xFF007A8A)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(name, fontWeight = FontWeight.Bold)
                        Text(status, fontSize = 12.sp, color = Color(0xFF627D98))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, description: String) {
    Column {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF005662))
        Text(description, fontSize = 14.sp, color = Color(0xFF627D98), lineHeight = 20.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun SettingsToggleItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F5F9),
        onClick = { onCheckedChange(!checked) }
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF102A43))
                Text(subtitle, fontSize = 13.sp, color = Color(0xFF627D98))
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF007A8A))
            )
        }
    }
}

@Composable
private fun SystemInfoRow(label: String, value: String, hasDot: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF455A64))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (hasDot) Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF4FC3F7)))
            Spacer(Modifier.width(8.dp))
            Text(value, fontSize = 13.sp, color = Color(0xFF455A64))
        }
    }
}

@Composable
private fun SettingsItemPro(icon: ImageVector, title: String, value: String, isBadge: Boolean = false, onClick: () -> Unit = {}) {
    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFF102A43), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(20.dp))
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334E68), modifier = Modifier.weight(1f))
            if (value.isNotEmpty()) {
                if (isBadge) {
                    Surface(color = Color(0xFFE0F2F1), shape = RoundedCornerShape(8.dp)) {
                        Text(value, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF007A8A), modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                } else Text(value, fontSize = 14.sp, color = Color(0xFF829AB1))
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFBCCCDC), modifier = Modifier.size(20.dp))
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun CurrentScanPreview() {
    val navController = rememberNavController()
    Dadn_appTheme {
        Scaffold(
            topBar = { ScaffoldCounterTopBar() },
            bottomBar = { ScaffoldCounterBottomNav(navController) }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize().background(Background)) {
                CurrentScanScreen()
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun SettingsPreview() {
    val navController = rememberNavController()
    Dadn_appTheme {
        Scaffold(
            topBar = { ScaffoldCounterTopBar() },
            bottomBar = { ScaffoldCounterBottomNav(navController) }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize().background(Background)) {
                SettingsScreen()
            }
        }
    }
}
