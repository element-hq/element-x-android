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
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.element.android.libraries.androidutils.system.openSystemOverlaySettings
import io.element.android.libraries.mediaviewer.impl.R
import io.element.android.libraries.mediaviewer.impl.floatingvideo.ui.FloatingVideoOverlay
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.MINIMIZED_EDGE_INSET_DP
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.dpToPx
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.getScreenHeight
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.maximizeWindowHelper
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.minimizeWindowHelper
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.movePosition
import io.element.android.libraries.mediaviewer.impl.floatingvideo.util.updateWindowSize
import timber.log.Timber

class FloatingVideoService : Service(), LifecycleOwner, SavedStateRegistryOwner {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var currentVideoUri: Uri? = null
    private var currentPositionMs: Long = 0L
    private val windowLayoutParams: WindowManager.LayoutParams = createLayoutParams()

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FLOATING -> {
                val uri = intent.data
                if (uri == null) {
                    Timber.tag(TAG).w("No video URI provided")
                    stopSelf()
                } else {
                    currentVideoUri = uri
                    currentPositionMs = intent.getLongExtra(EXTRA_POSITION, 0L)
                    createFloatingView()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun createFloatingView() {
        val uri = currentVideoUri ?: return
        removeFloatingView()
        val edgeInsetPx = dpToPx(MINIMIZED_EDGE_INSET_DP)
        windowLayoutParams.apply {
            gravity = Gravity.TOP or Gravity.START
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = edgeInsetPx
            y = windowManager.getScreenHeight() - dpToPx(INITIAL_FLOATING_WINDOW_OFFSET_Y_DP)
        }
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingVideoService)
            setViewTreeSavedStateRegistryOwner(this@FloatingVideoService)
            setContent {
                var isMinimized by remember { mutableStateOf(true) }
                FloatingVideoOverlay(
                    uri = uri,
                    startPositionMs = currentPositionMs,
                    isMinimized = isMinimized,
                    onClose = { dismissFloatingPlayer() },
                    onToggleFullScreen = { aspectRatio ->
                        if (isMinimized) {
                            isMinimized = false
                            maximizeWindowHelper(aspectRatio, windowManager, windowLayoutParams, floatingView)
                        } else {
                            isMinimized = true
                            minimizeWindowHelper(
                                aspectRatio,
                                windowManager,
                                windowLayoutParams,
                                floatingView,
                                dpToPx(MINIMIZED_EDGE_INSET_DP),
                            )
                        }
                    },
                    onComplete = { dismissFloatingPlayer() },
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

    private fun dismissFloatingPlayer() {
        currentVideoUri = null
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
        const val EXTRA_POSITION = "position"
        private const val INITIAL_FLOATING_WINDOW_OFFSET_Y_DP = 300

        @SuppressLint("ObsoleteSdkInt")
        fun startFloating(context: Context, videoUri: Uri, position: Long = 0L) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                Toast.makeText(
                    context,
                    context.getString(R.string.floating_video_overlay_permission_needed),
                    Toast.LENGTH_LONG,
                ).show()
                context.openSystemOverlaySettings()
                return
            }
            context.startService(
                Intent(context, FloatingVideoService::class.java).apply {
                    action = ACTION_START_FLOATING
                    data = videoUri
                    putExtra(EXTRA_POSITION, position)
                }
            )
        }
    }
}
