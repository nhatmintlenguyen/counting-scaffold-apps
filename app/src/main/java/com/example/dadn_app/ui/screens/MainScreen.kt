package com.example.dadn_app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dadn_app.data.local.ScanRecord
import com.example.dadn_app.ui.theme.*
import com.example.dadn_app.ui.viewmodel.HomeViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ─── Route constants ──────────────────────────────────────────────────────────

object Routes {
    const val INVENTORY    = "inventory"
    const val CURRENT_SCAN = "current_scan"
    const val PROCESSING   = "processing"
    const val RESULT       = "result"
    const val SETTINGS     = "settings"
    const val SECURITY     = "security"
    const val MFA          = "mfa"
    const val DEVICES      = "devices"
}

// ─── Nav item model ───────────────────────────────────────────────────────────

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector = icon,
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.INVENTORY,    "Inventory",    Icons.Outlined.Inventory2,      Icons.Filled.Inventory2),
    BottomNavItem(Routes.CURRENT_SCAN, "Current Scan", Icons.Outlined.DocumentScanner, Icons.Filled.DocumentScanner),
    BottomNavItem(Routes.SETTINGS,     "Settings",     Icons.Outlined.Settings,        Icons.Filled.Settings),
)

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun nowFormatted(): String =
    SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())

/**
 * Creates an empty image file in the app's cache and returns a FileProvider URI
 * that can be passed to ACTION_IMAGE_CAPTURE.
 */
private fun createCaptureUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera").also { it.mkdirs() }
    val file = File.createTempFile("capture_", ".jpg", dir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

/** Extracts a human-readable file extension from a content:// URI. */
private fun uriToFileType(context: Context, uri: Uri): String {
    val mime = context.contentResolver.getType(uri) ?: return ""
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)?.uppercase() ?: ""
}

// ─── Top App Bar ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldCounterTopBar() {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = TopBarBg),
        navigationIcon = {
            Row(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Architecture,
                    contentDescription = "Logo",
                    tint = Primary,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "S.C.P",
                    color = OnBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                )
            }
        },
        title = {},
        actions = {
            IconButton(
                onClick = {},
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Account",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        modifier = Modifier.shadow(2.dp)
    )
}

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────

@Composable
fun ScaffoldCounterBottomNav(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(
        containerColor = BottomBarBg,
        tonalElevation = 0.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .border(
                width = 0.5.dp,
                color = OutlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route ||
                    (item.route == Routes.CURRENT_SCAN &&
                            (currentRoute == Routes.PROCESSING || currentRoute == Routes.RESULT))
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            if (item.route == Routes.INVENTORY) {
                                popUpTo(Routes.INVENTORY) { inclusive = true }
                            } else {
                                popUpTo(Routes.INVENTORY) { inclusive = false }
                            }
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.iconSelected else item.icon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        maxLines = 1,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Primary,
                    selectedTextColor   = Primary,
                    unselectedIconColor = NavyMuted,
                    unselectedTextColor = NavyMuted,
                    indicatorColor      = Primary.copy(alpha = 0.08f),
                ),
            )
        }
    }
}

// ─── Navigation Graph ─────────────────────────────────────────────────────────

@Composable
fun ScaffoldCounterNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController    = navController,
        startDestination = Routes.INVENTORY,
        modifier         = modifier,
    ) {
        composable(Routes.INVENTORY)    { HomeScreen(navController) }
        composable(Routes.CURRENT_SCAN) { CurrentScanScreen() }
        composable(Routes.PROCESSING) {
            ProcessingScreen(
                onProcessingComplete = {
                    navController.navigate(Routes.RESULT) {
                        popUpTo(Routes.PROCESSING) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.RESULT)       { ResultScreen() }
        composable(Routes.SETTINGS)     { SettingsScreen(navController = navController, onLogout = onLogout) }
        composable(Routes.SECURITY)     { SecurityScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.MFA)          { MFAScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.DEVICES)      { DevicesScreen(onBack = { navController.popBackStack() }) }
    }
}

// ─── Main Screen (entry point) ────────────────────────────────────────────────

@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()

    Scaffold(
        topBar    = { ScaffoldCounterTopBar() },
        bottomBar = { ScaffoldCounterBottomNav(navController) },
        containerColor = Background,
    ) { innerPadding ->
        ScaffoldCounterNavGraph(
            navController = navController,
            onLogout      = onLogout,
            modifier      = Modifier.padding(innerPadding),
        )
    }
}

// ─── Home Screen ──────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    navController: NavHostController,
    vm: HomeViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scans by vm.scans.collectAsState()

    // ── State ────────────────────────────────────────────────────────────────
    // URI written just before launching the camera so the capture result callback can read it
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    // Controls the rationale/denied dialog
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    // Controls the upload-source bottom sheet
    var showUploadSheet by remember { mutableStateOf(false) }

    // ── Activity result launchers ─────────────────────────────────────────────

    // 1. System camera — TakePicture writes the photo to pendingCaptureUri
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val uri = pendingCaptureUri ?: return@rememberLauncherForActivityResult
            vm.addScan(
                ScanRecord(
                    name      = "Field Capture",
                    datetime  = nowFormatted(),
                    fileType  = "JPG",
                    status    = "Pending",
                    imageUri  = uri.toString(),
                )
            )
            navController.navigate(Routes.PROCESSING) {
                launchSingleTop = true
            }
        }
    }

    // 2a. System PhotoPicker (Photos app) — no permission needed on API 33+
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            vm.addScan(
                ScanRecord(
                    name      = "Gallery Import",
                    datetime  = nowFormatted(),
                    fileType  = uriToFileType(context, uri),
                    status    = "Pending",
                    imageUri  = uri.toString(),
                )
            )
            navController.navigate(Routes.PROCESSING) { launchSingleTop = true }
        }
    }

    // 2b. Files browser — OpenDocument filtered to image MIME types only
    //     This covers Downloads, Google Drive, local storage, etc.
    val filesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            vm.addScan(
                ScanRecord(
                    name      = "File Import",
                    datetime  = nowFormatted(),
                    fileType  = uriToFileType(context, uri),
                    status    = "Pending",
                    imageUri  = uri.toString(),
                )
            )
            navController.navigate(Routes.PROCESSING) { launchSingleTop = true }
        }
    }

    // 3. Camera permission request
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingCaptureUri = createCaptureUri(context)
            cameraLauncher.launch(pendingCaptureUri!!)
        } else {
            showCameraPermissionDialog = true
        }
    }

    // ── Camera permission denied dialog ────────────────────────────────────
    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = Primary
                )
            },
            title = { Text("Camera access needed") },
            text  = {
                Text(
                    "S.C.P needs camera access to capture scaffold images for counting. " +
                    "Please grant the Camera permission in your device Settings."
                )
            },
            confirmButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) {
                    Text("OK", color = Primary)
                }
            },
            containerColor = SurfaceContainerLowest,
        )
    }

    // ── Actions ──────────────────────────────────────────────────────────────
    val onTakePhoto: () -> Unit = {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {
                pendingCaptureUri = createCaptureUri(context)
                cameraLauncher.launch(pendingCaptureUri!!)
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Tapping "Upload Gallery" opens the source picker sheet instead of launching directly
    val onUploadGallery: () -> Unit = { showUploadSheet = true }

    // ── Upload source bottom sheet ────────────────────────────────────────────
    if (showUploadSheet) {
        UploadSourceSheet(
            onDismiss = { showUploadSheet = false },
            onPickFromPhotos = {
                showUploadSheet = false
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onBrowseFiles = {
                showUploadSheet = false
                // Pass all common image MIME types so only images are shown in the file browser
                filesLauncher.launch(
                    arrayOf("image/jpeg", "image/png", "image/webp", "image/gif", "image/bmp", "image/heic")
                )
            },
        )
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
    ) {
        item { HeroSection() }
        item { Spacer(Modifier.height(28.dp)) }
        item { ActionGrid(onTakePhoto = onTakePhoto, onUploadGallery = onUploadGallery) }
        item { Spacer(Modifier.height(28.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Scans",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface,
                    letterSpacing = (-0.3).sp,
                )
                if (scans.isNotEmpty()) {
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.clickable {}
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        if (scans.isEmpty()) {
            item { EmptyScansPlaceholder() }
        } else {
            items(scans) { scan ->
                RecentScanCard(scan)
                Spacer(Modifier.height(12.dp))
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
        item { MetadataStrip() }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyScansPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.ImageSearch,
            contentDescription = null,
            tint = OutlineVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No scans yet",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
        )
        Text(
            text = "Take a photo or upload from gallery\nto start counting.",
            fontSize = 13.sp,
            color = NavyMuted,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
        )
    }
}

// ── Upload Source Bottom Sheet ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadSourceSheet(
    onDismiss: () -> Unit,
    onPickFromPhotos: () -> Unit,
    onBrowseFiles: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceContainerLowest,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        ) {
            Text(
                text       = "Choose image source",
                fontSize   = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = OnSurface,
            )
            Text(
                text     = "Select where to import your scaffold image from.",
                fontSize = 13.sp,
                color    = OnSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )

            UploadSourceOption(
                icon        = Icons.Filled.PhotoLibrary,
                title       = "Photos",
                description = "Pick from the Photos app on your device",
                onClick     = onPickFromPhotos,
            )

            HorizontalDivider(
                modifier  = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color     = OutlineVariant.copy(alpha = 0.4f),
            )

            UploadSourceOption(
                icon        = Icons.Filled.Folder,
                title       = "Browse Files",
                description = "Open Downloads, Drive, or any local folder — images only",
                onClick     = onBrowseFiles,
            )
        }
    }
}

@Composable
private fun UploadSourceOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
            Text(description, fontSize = 12.sp, color = OnSurfaceVariant, lineHeight = 17.sp)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

// ── Hero Section ──────────────────────────────────────────────────────────────

@Composable
private fun HeroSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Construction Ready v4.2",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 1.5.sp,
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Precision Scaffold",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = OnSurface,
            letterSpacing = (-1).sp,
        )
        Text(
            text = "Counting",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic,
            color = Primary,
            letterSpacing = (-1).sp,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "High-fidelity computer vision for rapid inventory auditing. Select an input method to begin your automated count.",
            fontSize = 14.sp,
            color = OnSurfaceVariant,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

// ── Action Grid ───────────────────────────────────────────────────────────────

@Composable
private fun ActionGrid(
    onTakePhoto: () -> Unit,
    onUploadGallery: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TakePhotoCard(modifier = Modifier.fillMaxWidth(), onClick = onTakePhoto)
        UploadGalleryCard(modifier = Modifier.fillMaxWidth(), onClick = onUploadGallery)
    }
}

@Composable
private fun TakePhotoCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(colors = listOf(Primary, PrimaryContainer)))
            .clickable(onClick = onClick)
            .padding(28.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.PhotoCamera,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.size(140.dp).align(Alignment.TopEnd)
        )
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AddAPhoto,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text("Take Photo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(
                "Open field camera for real-time processing.",
                fontSize = 12.sp,
                color = PrimaryFixed.copy(alpha = 0.85f),
                lineHeight = 17.sp,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "LAUNCH CAMERA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.2.sp,
                )
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun UploadGalleryCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .border(0.5.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(28.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CloudUpload,
            contentDescription = null,
            tint = SurfaceContainerHighest,
            modifier = Modifier.size(100.dp).align(Alignment.TopEnd)
        )
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Primary.copy(alpha = 0.06f))
                    .border(0.5.dp, Primary.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.FolderOpen, null, tint = Primary, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Upload Gallery", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(Modifier.height(4.dp))
            Text("Import photos for batch analysis.", fontSize = 12.sp, color = OnSurfaceVariant, lineHeight = 17.sp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("BROWSE FILES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary, letterSpacing = 1.2.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Filled.AttachFile, null, tint = Primary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ── Recent Scan Card ──────────────────────────────────────────────────────────

@Composable
private fun RecentScanCard(scan: ScanRecord) {
    val isArchived = scan.status == "Archived"
    val isPending  = scan.status == "Pending"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .border(0.5.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable {}
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail placeholder
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.BottomEnd
        ) {
            if (scan.fileType.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Primary.copy(alpha = 0.8f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(scan.fileType, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = scan.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isArchived) OnSurface.copy(alpha = 0.6f) else OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(scan.datetime, fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
        }

        Column(horizontalAlignment = Alignment.End) {
            val (badgeBg, badgeText) = when (scan.status) {
                "Success"  -> Pair(Color(0xFFCCF0F8), Color(0xFF005270))
                "Pending"  -> Pair(Color(0xFFFFF3CD), Color(0xFF7A5200))
                "Archived" -> Pair(SurfaceContainerHigh, OnSurfaceVariant)
                else       -> Pair(SurfaceContainerHigh, OnSurfaceVariant)
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = scan.status.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeText,
                    letterSpacing = 0.5.sp,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (isPending) "—" else "${scan.count}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isArchived || isPending) NavyMuted else Primary,
                    lineHeight = 20.sp,
                )
                if (!isPending) {
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "pcs",
                        fontSize = 9.sp,
                        color = if (isArchived) NavyMuted else OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
            }
        }

        Icon(Icons.Filled.ChevronRight, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

// ── Metadata Strip ────────────────────────────────────────────────────────────

@Composable
private fun MetadataStrip() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MetadataCard(icon = null,                  glowDot = true,  label = "System Status",    value = "Engine: Neural-Count v2.1 (Active)")
        MetadataCard(icon = Icons.Filled.Storage,  glowDot = false, label = "Sync Progress",    value = "Local Cache: 12 Scans Pending")
        MetadataCard(icon = Icons.Filled.Memory,   glowDot = false, label = "Device Efficiency",value = "Neural Processing: Optimized")
    }
}

@Composable
private fun MetadataCard(icon: ImageVector?, glowDot: Boolean, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (glowDot) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(PrimaryFixedDim))
        } else if (icon != null) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp))
        }
        Column {
            Text(label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
        }
    }
}

// ─── Placeholder Screens ──────────────────────────────────────────────────────

@Composable
fun CurrentScanScreen() {
    NoScanScreen()
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center,
    ) {
        Text("$label — Coming Soon", color = NavyMuted, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    Dadn_appTheme { MainScreen() }
}
