/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.zacsweers.metro.Inject
import io.element.android.libraries.androidutils.system.openSystemOverlaySettings
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.mediaviewer.impl.floatingvideo.ui.FloatingVideoOverlay
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.MINIMIZED_EDGE_INSET_DP
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.dpToPx
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.getScreenHeight
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.getUri
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.maximizeWindowHelper
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.minimizeWindowHelper
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.movePosition
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.updateWindowSize
import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerPageData
import timber.log.Timber

class FloatingVideoService : Service(), LifecycleOwner, SavedStateRegistryOwner {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var currentVideoData: MediaViewerPageData.MediaViewerData? = null
    private var currentPositionMs: Long = 0L
    private var isMinimized = true
    private var currentVideoId: String? = null
    private lateinit var windowLayoutParams: WindowManager.LayoutParams

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    @Inject lateinit var videoDataRepository: VideoDataRepository

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        bindings<FloatingVideoServiceBindings>().inject(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FLOATING -> {
                val videoId = intent.getStringExtra(EXTRA_VIDEO_ID) ?: return START_NOT_STICKY
                val position = intent.getLongExtra(EXTRA_POSITION, 0L)
                val videoData = videoDataRepository.getVideoData(videoId)
                if (videoData == null) {
                    Timber.tag(TAG).w("No video data for id=%s", videoId)
                    stopSelf()
                } else {
                    currentVideoData = videoData
                    currentVideoId = videoId
                    currentPositionMs = position
                    createFloatingView()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun createFloatingView() {
        removeFloatingView()
        val edgeInsetPx = dpToPx(MINIMIZED_EDGE_INSET_DP)
        windowLayoutParams = createLayoutParams().apply {
            gravity = Gravity.TOP or Gravity.START
            x = edgeInsetPx
            y = windowManager.getScreenHeight() - dpToPx(INITIAL_FLOATING_WINDOW_OFFSET_Y_DP)
        }
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingVideoService)
            setViewTreeSavedStateRegistryOwner(this@FloatingVideoService)
            setContent {
                FloatingVideoOverlay(
                    uri = currentVideoData.getUri(),
                    startPositionMs = currentPositionMs,
                    onClose = { dismissFloatingPlayer() },
                    onToggleFullScreen = { aspectRatio ->
                        if (isMinimized) maximizeWindow(aspectRatio) else minimizeWindow(aspectRatio)
                    },
                    onCompleted = { dismissFloatingPlayer() },
                    updateAspectRatio = { aspectRatio ->
                        updateWindowSize(aspectRatio, isMinimized, windowManager, windowLayoutParams, floatingView)
                    },
                    movePosition = { x, y ->
                        movePosition(x, y, windowLayoutParams, floatingView, windowManager)
                    },
                )
            }
        }
        floatingView = composeView
        try {
            windowManager?.addView(floatingView, windowLayoutParams)
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error adding floating view")
            dismissFloatingPlayer()
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT,
        )
    }

    private fun minimizeWindow(aspectRatio: Float) {
        isMinimized = true
        minimizeWindowHelper(aspectRatio, windowManager, windowLayoutParams, floatingView, dpToPx(MINIMIZED_EDGE_INSET_DP))
    }

    private fun maximizeWindow(aspectRatio: Float) {
        isMinimized = false
        maximizeWindowHelper(aspectRatio, windowManager, windowLayoutParams, floatingView)
    }

    private fun dismissFloatingPlayer() {
        currentVideoId?.let { videoDataRepository.removeVideoData(it) }
        currentVideoId = null
        currentVideoData = null
        removeFloatingView()
        stopSelf()
    }

    private fun removeFloatingView() {
        floatingView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error removing floating view")
            }
            floatingView = null
        }
    }

    override fun onDestroy() {
        removeFloatingView()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
    }

    companion object {
        private const val TAG = "FloatingVideoService"
        const val ACTION_START_FLOATING = "START_FLOATING"
        const val EXTRA_VIDEO_ID = "video_id"
        const val EXTRA_POSITION = "position"
        private const val INITIAL_FLOATING_WINDOW_OFFSET_Y_DP = 300

        @SuppressLint("ObsoleteSdkInt")
        fun startFloating(context: Context, videoId: String, position: Long = 0L) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                // TODO: move this copy to CommonStrings / Localazy once the string is approved.
                Toast.makeText(
                    context,
                    "To show the floating video, please allow 'Display over other apps' permission.",
                    Toast.LENGTH_LONG,
                ).show()
                context.openSystemOverlaySettings()
                return
            }
            context.startService(
                Intent(context, FloatingVideoService::class.java).apply {
                    action = ACTION_START_FLOATING
                    putExtra(EXTRA_VIDEO_ID, videoId)
                    putExtra(EXTRA_POSITION, position)
                }
            )
        }
    }
}
