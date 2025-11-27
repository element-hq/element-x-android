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
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerPageData
import timber.log.Timber
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.element.android.libraries.androidutils.system.openSystemOverlaySettings
import io.element.android.libraries.mediaviewer.impl.floatingvideo.ui.FloatingVideoOverlay
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.getScreenHeight
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.dpToPx
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.getUri
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.maximizeWindowHelper
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.minimizeWindowHelper
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.movePosition
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.updateWindowSize

class FloatingVideoService : Service(), LifecycleOwner, SavedStateRegistryOwner {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var videoView: VideoView? = null
    private var currentVideoData: MediaViewerPageData.MediaViewerData? = null
    private var currentPosition: Long = 0L
    private var isMinimized = true

    private lateinit var windowLayoutParams: WindowManager.LayoutParams

    companion object {
        const val ACTION_START_FLOATING = "START_FLOATING"
        const val EXTRA_VIDEO_ID = "video_id"
        const val EXTRA_POSITION = "position"

        private const val INITIAL_FLOATING_WINDOW_OFFSET_Y = 300

        @SuppressLint("ObsoleteSdkInt")
        fun startFloating(
            context: Context, videoId: String, position: Long = 0L
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {

                //the message needs to be added into commonStrings as notice for permission needed
                Toast.makeText(context, "To show the floating video, please allow 'Display over other apps' permission.", Toast.LENGTH_LONG).show()

                // Request overlay permission
                context.openSystemOverlaySettings()
                return
            }

            val intent = Intent(context, FloatingVideoService::class.java).apply {
                action = ACTION_START_FLOATING
                putExtra(EXTRA_VIDEO_ID, videoId)
                putExtra(EXTRA_POSITION, position)
            }
            context.startService(intent)
        }
    }

    @Inject lateinit var videoDataRepository: VideoDataRepository

    override fun onBind(intent: Intent?): IBinder? = null

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
        }
        return START_STICKY
    }

    private fun createFloatingView() {
        removeFloatingView()

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
        windowLayoutParams.y = windowManager.getScreenHeight() - dpToPx(INITIAL_FLOATING_WINDOW_OFFSET_Y)

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingVideoService)
            setViewTreeSavedStateRegistryOwner(this@FloatingVideoService)
            setContent {
                FloatingVideoOverlay(
                    onClose = {
                        removeFloatingView()
                        stopSelf()
                    },
                    onToggleFullScreen = {aspectRatio ->
                        if (isMinimized) {
                            maximizeWindow(aspectRatio)
                        } else {
                            minimizeWindow(aspectRatio)
                        }
                    },
                    onCompleted = {
                        removeFloatingView()
                        stopSelf()
                    },
                    updateAspectRatio = {
                        updateWindowSize(
                            aspectRatio = it,
                            isMinimized = isMinimized,
                            floatingView = floatingView,
                            windowManager = windowManager,
                            windowLayoutParams = windowLayoutParams
                        )
                    },
                    uri = currentVideoData.getUri(),
                    movePosition = { x, y ->
                        movePosition(x = x, y = y, windowLayoutParams = windowLayoutParams, floatingView = floatingView, windowManager = windowManager)
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

    override fun onDestroy() {
        super.onDestroy()
        onVideoComplete()
    }

    private fun onVideoComplete() {
        removeFloatingView()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    private fun minimizeWindow(aspectRatio: Float) {
        isMinimized = true
        minimizeWindowHelper(
            aspectRatio = aspectRatio,
            windowManager = windowManager,
            windowLayoutParams = windowLayoutParams,
            floatingView = floatingView
        )
    }
    private fun maximizeWindow(aspectRatio: Float) {
        isMinimized = false
        maximizeWindowHelper(
            aspectRatio = aspectRatio,
            windowManager = windowManager,
            windowLayoutParams = windowLayoutParams,
            floatingView = floatingView
        )
    }
}
