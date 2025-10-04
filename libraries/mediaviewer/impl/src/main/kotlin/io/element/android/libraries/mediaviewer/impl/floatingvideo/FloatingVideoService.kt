/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.floatingvideo

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.VideoView
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerPageData
import timber.log.Timber
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.ui.strings.CommonStrings
import java.io.File
import androidx.core.net.toUri

class FloatingVideoService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var videoView: VideoView? = null
    private var currentVideoData: MediaViewerPageData.MediaViewerData? = null
    private var currentPosition: Long = 0L
    private var isMaximized = true

    private lateinit var windowLayoutParams: WindowManager.LayoutParams

    companion object {
        const val ACTION_START_FLOATING = "START_FLOATING"
        const val ACTION_STOP_FLOATING = "STOP_FLOATING"
        const val ACTION_UPDATE_POSITION = "UPDATE_POSITION"
        const val EXTRA_VIDEO_ID = "video_id"
        const val EXTRA_POSITION = "position"

        @SuppressLint("ObsoleteSdkInt")
        fun startFloating(
            context: Context, videoData: MediaViewerPageData.MediaViewerData, position: Long = 0L
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                // Request overlay permission
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = "package:${context.packageName}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }


                context.startActivity(intent)
                return
            }

            // Generate unique ID for this video session
            val videoId = "floating_video_${System.currentTimeMillis()}"

            // Store the video data in repository
            VideoDataRepository.getInstance().storeVideoData(videoId, videoData)

            val intent = Intent(context, FloatingVideoService::class.java).apply {
                action = ACTION_START_FLOATING
                putExtra(EXTRA_VIDEO_ID, videoId)  // Pass only the ID, not the whole object
                putExtra(EXTRA_POSITION, position)
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override val viewModelStore = ViewModelStore()
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        // 1. Attach controller
        savedStateRegistryController.performAttach()

        // 2. Restore state (if any)
        savedStateRegistryController.performRestore(null)

        // 3. Now move lifecycle forward
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    private var currentVideoId: String? = null
    private var eventId: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FLOATING -> {
                val videoId = intent.getStringExtra(EXTRA_VIDEO_ID)
                val position = intent.getLongExtra(EXTRA_POSITION, 0L)

                if (videoId != null) {
                    // Get video data from repository using the ID
                    val videoData = VideoDataRepository.getInstance().getVideoData(videoId)
                    if (videoData != null) {
                        eventId = videoData.eventId?.value ?: ""
                        currentVideoData = videoData
                        currentVideoId = videoId
                        currentPosition = position
                        createFloatingView()
                    }
                }
            }

            ACTION_STOP_FLOATING -> {
                // Clean up stored data
                currentVideoId?.let { videoId ->
                    VideoDataRepository.getInstance().removeVideoData(videoId)
                }
                removeFloatingView()
                stopSelf()
            }

            ACTION_UPDATE_POSITION -> {
                val position = intent.getLongExtra(EXTRA_POSITION, 0L)
                currentPosition = position
                videoView?.seekTo(position.toInt())
            }
        }
        return START_STICKY
    }

    private fun createFloatingView() {
        removeFloatingView()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        windowLayoutParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            )
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            )
        }

        windowLayoutParams.gravity = Gravity.TOP or Gravity.START
        windowLayoutParams.x = 0
        windowLayoutParams.y = getScreenHeight() - dpToPx(300)

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingVideoService)
            setViewTreeViewModelStoreOwner(this@FloatingVideoService)
            setViewTreeSavedStateRegistryOwner(this@FloatingVideoService)
            setContent {
                FloatingVideoOverlay(
                    onClose = {
                        removeFloatingView()
                        stopSelf()
                    },
                    onToggleFullScreen = {
                        Timber.tag("onToggleFullScreen").d(isMaximized.toString())
                        toggleFullScreen()
                    }
                )
            }
        }


        floatingView = composeView


        try {
            windowManager?.addView(floatingView, windowLayoutParams)
        } catch (e: Exception) {
            Timber.tag("FloatingVideoService").e(e, "Error adding floating view")
        }
    }

    private fun removeFloatingView() {
        floatingView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                Timber.tag("FloatingVideoService").e(e, "Error removing floating view")
            }
            floatingView = null
            videoView = null
        }
    }

    @Composable
    fun FloatingVideoOverlay(
        onClose: () -> Unit,
        onToggleFullScreen: () -> Unit
    ) {
        var currentAspectRatio by remember { mutableStateOf(16f / 9f) }
        val videoViewRef = remember { mutableStateOf<VideoView?>(null) }



        var resolvedUri: Uri = Uri.EMPTY
        currentVideoData?.let { data ->
            resolvedUri = when (val downloadedState = data.downloadedMedia.value) {
                is AsyncData.Success -> downloadedState.data.uri
                else -> getVideoUriFromMediaSource(data.mediaSource)
            }
        }

        // Function to update window size directly
        fun updateWindowSize(aspectRatio: Float) {
            val widthFrac = if (aspectRatio > 1f) 0.6f else 0.3f
            val width = if (isMaximized) {
                (getScreenWidth() * widthFrac).toInt()
            } else {
                (getScreenWidth() * 0.9f).toInt()
            }
            val height = (width / aspectRatio).toInt()

            Timber.tag("WindowUpdate").d("Updating window - width: $width, height: $height, aspectRatio: $aspectRatio")

            windowLayoutParams.width = width
            windowLayoutParams.height = height
            windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            windowManager?.updateViewLayout(floatingView, windowLayoutParams)
        }

        // Initial window size (16:9)
        LaunchedEffect(Unit) {
            updateWindowSize(16f / 9f)
        }

        // Update window size when isMaximized changes
        LaunchedEffect(isMaximized) {
            Timber.tag("MaximizeToggle").d("isMaximized changed to: $isMaximized, updating with aspectRatio: $currentAspectRatio")
            updateWindowSize(currentAspectRatio)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = androidx.compose.ui.graphics.Color.Black).pointerInput(Unit) {
                    var dragStarted = false
                    detectTapGestures(
                        onPress = {
                            dragStarted = false
                        },
                        onTap = {
                            if (!dragStarted) {
                                videoViewRef.value?.let { video ->
                                    if (video.isPlaying) {
                                        video.pause()
                                    } else {
                                        video.start()
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            // Video layer
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        videoViewRef.value = this
                        setVideoURI(resolvedUri)
                        setOnPreparedListener { mp ->
                            val videoWidth = mp.videoWidth
                            val videoHeight = mp.videoHeight

                            if (videoWidth > 0 && videoHeight > 0) {
                                val newAspectRatio = videoWidth.toFloat() / videoHeight

                                // Store the aspect ratio and update window size
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    currentAspectRatio = newAspectRatio
                                    updateWindowSize(newAspectRatio)
                                }
                            }
                            start()
                        }

                    }
                },
                update = { videoView ->
                    if (resolvedUri != Uri.EMPTY && videoView.currentPosition == 0) {
                        videoView.setVideoURI(resolvedUri)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()

            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newX = windowLayoutParams.x + dragAmount.x.toInt()
                            val newY = windowLayoutParams.y + dragAmount.y.toInt()
                            windowLayoutParams.x = newX
                            windowLayoutParams.y = newY
                            windowManager?.updateViewLayout(floatingView, windowLayoutParams)
                        }
                    }
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                                androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        onToggleFullScreen()
                        updateWindowSize(currentAspectRatio)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        //action full screen needs to be added to CommonsString
                        contentDescription = stringResource(CommonStrings.action_view),
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(CommonStrings.action_close),
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingView()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun toggleFullScreen() {
        isMaximized = !isMaximized

        if (floatingView?.parent == null) return

        if (!isMaximized) {
            // Full screen with margin
            val margin = dpToPx(24)

            // shrink the container size by margins
            windowLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            windowLayoutParams.x = 0
            windowLayoutParams.y = 0

            // video fills the container, container itself is inset by margins
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels

            windowLayoutParams.width = screenWidth - margin * 2
            windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            windowLayoutParams.x = margin
            windowLayoutParams.y = margin
        } else {
            val scaledWidth = getScreenWidth() * 0.3f

            windowLayoutParams.width = scaledWidth.toInt()
            windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            windowLayoutParams.x = 0
            windowLayoutParams.y = 0
        }

        windowManager?.updateViewLayout(floatingView, windowLayoutParams)
    }

    private fun getScreenWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager?.currentWindowMetrics
            windowMetrics?.bounds?.width() ?: 0
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION") windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    private fun getScreenHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager?.currentWindowMetrics
            windowMetrics?.bounds?.height() ?: 0
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION") windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }
}

@OptIn(UnstableApi::class)
fun getVideoUriFromMediaSource(mediaSource: MediaSource): Uri {
    return try {
        val url = mediaSource.url
        Log.d("VideoPlayer", "MediaSource URL: $url")

        when {
            url.startsWith("http://") || url.startsWith("https://") -> {
                // Remote URL
                url.toUri()
            }
            url.startsWith("file://") -> {
                // Already a file URI
                url.toUri()
            }
            url.startsWith("/") -> {
                // Local file path, convert to file URI
                Uri.fromFile(File(url))
            }
            url.startsWith("content://") -> {
                // Content URI (from MediaStore, etc.)
                url.toUri()
            }
            else -> {
                Log.w("VideoPlayer", "Unknown URL format: $url")
                // Try parsing as-is, might work
                url.toUri()
            }
        }
    } catch (e: Exception) {
        Timber.tag("Uri Parsing").e(e)
        Uri.EMPTY
    }
}
