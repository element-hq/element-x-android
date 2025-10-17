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
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.VideoView
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerPageData
import timber.log.Timber
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.core.net.toUri
import io.element.android.libraries.mediaviewer.impl.floatingvideo.ui.FloatingVideoOverlay
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.getScreenHeight
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.getScreenWidth
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.bindings

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

            // Store the video data in repository via DI
            context.bindings<FloatingVideoServiceBindings>().videoDataRepository().storeVideoData(videoId, videoData)

            val intent = Intent(context, FloatingVideoService::class.java).apply {
                action = ACTION_START_FLOATING
                putExtra(EXTRA_VIDEO_ID, videoId)  // Pass only the ID, not the whole object
                putExtra(EXTRA_POSITION, position)
            }
            context.startService(intent)
        }
    }

    @Inject lateinit var videoDataRepository: VideoDataRepository

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
        bindings<FloatingVideoServiceBindings>().inject(this)
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
                    val videoData = videoDataRepository.getVideoData(videoId)
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
                    videoDataRepository.removeVideoData(videoId)
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
        windowLayoutParams.y = windowManager.getScreenHeight() - dpToPx(300)

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
                        this@FloatingVideoService.toggleFullScreen(it)                    },
                    onCompleted = {
                        removeFloatingView()
                        stopSelf()
                    },
                    floatingView = floatingView,
                    isMaximized = isMaximized ,
                    currentVideoData = currentVideoData,
                    windowManager = windowManager,
                    windowLayoutParams = windowLayoutParams
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

    override fun onDestroy() {
        super.onDestroy()
        onVideoComplete()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun onVideoComplete(){
        removeFloatingView()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
    }

    private fun toggleFullScreen( aspectRatio : Float ) {
        val layoutParams = windowLayoutParams
        val wm = windowManager ?: return
        val view = floatingView ?: return

        isMaximized = !isMaximized

        if (view.parent == null) return


        val widthFrac = if (aspectRatio > 1f) 0.6f else 0.3f
        val width = if (isMaximized) {
            (windowManager.getScreenWidth() * widthFrac).toInt()
        } else {
            (windowManager.getScreenWidth() * 0.9f).toInt()
        }
        val height = (width / aspectRatio).toInt()




        if (isMaximized) {
            // Go full screen
            val margin = dpToPx(24)
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            layoutParams.width = screenWidth - margin * 2
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.x = margin
            layoutParams.y = margin
        } else {
            // Minimized
            val scaledWidth = wm.getScreenWidth() * 0.3f
            layoutParams.width = scaledWidth.toInt()
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.x = 0
            layoutParams.y = 0
        }

        windowLayoutParams.width = width
        windowLayoutParams.height = height
        windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        windowManager?.updateViewLayout(floatingView, windowLayoutParams)

        Handler(Looper.getMainLooper()).post {
            wm.updateViewLayout(view, layoutParams)
        }

    }

}
