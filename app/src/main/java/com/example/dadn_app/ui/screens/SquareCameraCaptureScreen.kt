package com.example.dadn_app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.view.Surface
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.dadn_app.core.utils.ScanImageProcessor
import com.example.dadn_app.core.utils.SquareCropSpec
import com.example.dadn_app.ui.theme.Primary
import java.util.concurrent.Executors

@Composable
fun SquareCameraCaptureScreen(
    onImageCaptured: (Uri) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = remember(context) { context.findLifecycleOwner() }
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
    }

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    capture,
                )
                imageCapture = capture
            } catch (_: Exception) {
                Toast.makeText(context, "Unable to start camera", Toast.LENGTH_SHORT).show()
            }
        }

        cameraProviderFuture.addListener(listener, mainExecutor)

        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
            cameraExecutor.shutdown()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        val density = LocalDensity.current
        val frameSize = min(maxWidth, maxHeight) * 0.78f
        val frameLeft = (maxWidth - frameSize) / 2f
        val frameTop = (maxHeight - frameSize) / 2f
        val cropSpec = with(density) {
            SquareCropSpec(
                previewWidthPx = maxWidth.roundToPx(),
                previewHeightPx = maxHeight.roundToPx(),
                frameLeftPx     = frameLeft.roundToPx(),
                frameTopPx = frameTop.roundToPx(),
                frameSizePx = frameSize.roundToPx(),
            )
        }

        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )

        SquareCameraOverlay(
            frameLeftPx = with(density) { frameLeft.toPx() },
            frameTopPx = with(density) { frameTop.toPx() },
            frameSizePx = with(density) { frameSize.toPx() },
        )

        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.35f), CircleShape),
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close camera", tint = Color.White)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.42f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Align the scaffold inside the square",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    enabled = !isCapturing,
                    onClick = {
                        val capture = imageCapture ?: return@IconButton
                        isCapturing = true
                        val originalFile = ScanImageProcessor.createOriginalCaptureFile(context)
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(originalFile).build()

                        capture.takePicture(
                            outputOptions,
                            cameraExecutor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    runCatching {
                                        ScanImageProcessor.cropToOverlaySquareAndResize(
                                            context = context,
                                            originalFile = originalFile,
                                            cropSpec = cropSpec,
                                        )
                                    }.onSuccess { processedUri ->
                                        mainExecutor.execute {
                                            isCapturing = false
                                            onImageCaptured(processedUri)
                                        }
                                    }.onFailure {
                                        mainExecutor.execute {
                                            isCapturing = false
                                            Toast.makeText(context, "Unable to process image", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    mainExecutor.execute {
                                        isCapturing = false
                                        Toast.makeText(context, "Unable to capture image", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                        )
                    },
                    modifier = Modifier
                        .size(76.dp)
                        .background(Color.White, CircleShape)
                        .border(4.dp, Primary, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Capture image",
                        tint = Primary,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SquareCameraOverlay(
    frameLeftPx: Float,
    frameTopPx: Float,
    frameSizePx: Float,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val dim = Color.Black.copy(alpha = 0.48f)
        drawRect(dim, topLeft = Offset.Zero, size = Size(size.width, frameTopPx))
        drawRect(
            dim,
            topLeft = Offset(0f, frameTopPx + frameSizePx),
            size = Size(size.width, size.height - frameTopPx - frameSizePx),
        )
        drawRect(dim, topLeft = Offset(0f, frameTopPx), size = Size(frameLeftPx, frameSizePx))
        drawRect(
            dim,
            topLeft = Offset(frameLeftPx + frameSizePx, frameTopPx),
            size = Size(size.width - frameLeftPx - frameSizePx, frameSizePx),
        )
        drawRect(
            color = Color.White,
            topLeft = Offset(frameLeftPx, frameTopPx),
            size = Size(frameSizePx, frameSizePx),
            style = Stroke(width = 4.dp.toPx()),
        )
        drawRect(
            color = Primary,
            topLeft = Offset(frameLeftPx + 8.dp.toPx(), frameTopPx + 8.dp.toPx()),
            size = Size(frameSizePx - 16.dp.toPx(), frameSizePx - 16.dp.toPx()),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Context.findLifecycleOwner(): LifecycleOwner {
    return findActivity() as? LifecycleOwner
        ?: error("Camera screen requires a LifecycleOwner context")
}
