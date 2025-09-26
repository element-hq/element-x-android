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
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import androidx.annotation.OptIn
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerPageData
import timber.log.Timber
import kotlin.math.abs
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.impl.R
import java.io.File

class FloatingVideoService : Service() {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var videoView: VideoView? = null
    private var closeButton: ImageView? = null
    private var maximizeButton: ImageView? = null
    private var playPauseButton: ImageView? = null
    private var overlayContainer: FrameLayout? = null
    private var currentVideoData: MediaViewerPageData.MediaViewerData? = null
    private var currentPosition: Long = 0L
    private var isMaximized = false
    private var seekBar: SeekBar? = null
    private var progressHandler = Handler(Looper.getMainLooper())

    private var overlayHandler = Handler(Looper.getMainLooper())
    private var controlsVisibility: Boolean = false

    private lateinit var layoutParams: WindowManager.LayoutParams

    companion object {
        const val ACTION_START_FLOATING = "START_FLOATING"
        const val ACTION_STOP_FLOATING = "STOP_FLOATING"
        const val ACTION_UPDATE_POSITION = "UPDATE_POSITION"
        const val EXTRA_VIDEO_ID = "video_id"  // Changed from EXTRA_VIDEO_DATA
        const val EXTRA_POSITION = "position"

        @SuppressLint("ObsoleteSdkInt")
        fun startFloating(
            context: Context, videoData: MediaViewerPageData.MediaViewerData, position: Long = 0L
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                // Request overlay permission
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
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

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
        floatingView = createFloatingVideoLayout()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val fixedHeight = dpToPx(200)
        layoutParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                dpToPx(150),
                fixedHeight,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                dpToPx(150),
                fixedHeight,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            )
        }

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = getScreenWidth() - dpToPx(150) - dpToPx(16)
        layoutParams.y = getScreenHeight() - fixedHeight - dpToPx(100)

        setupTouchListener(layoutParams)

        try {
            windowManager?.addView(floatingView, layoutParams)

            setupVideo(layoutParams, fixedHeight)
        } catch (e: Exception) {
            Timber.tag("FloatingVideoService").e(e, "Error adding floating view")
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingVideoLayout(): ViewGroup {
        var cornerRadius = dpToPx(0).toFloat()

        val container = FrameLayout(this).apply {
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            clipToOutline = true // This enables corner clipping

            background = GradientDrawable().apply {
                setColor(Color.BLACK) // Optional: background behind video
                this.cornerRadius = cornerRadius
            }
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,

                )
        }
        videoView = VideoView(this).apply {
            id = View.generateViewId()
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(2, 2, 2, 2)
            }

        }

        container.addView(videoView)
        overlayContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            layoutDirection = View.LAYOUT_DIRECTION_LTR

            // Semi-transparent black background
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#80000000")) // 50% transparent black
            }

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, dpToPx(32)
            )

            isFocusable = false
            isFocusableInTouchMode = false
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            clearFocus()
            // Initially visible, you can hide/show this container for auto-hide functionality
            visibility = View.VISIBLE
        }
        closeButton = ImageView(this).apply {
            setImageResource(R.drawable.ic_close)
            setColorFilter(Color.WHITE)



            layoutParams = FrameLayout.LayoutParams(dpToPx(24), dpToPx(24)).apply {
                gravity = Gravity.TOP or Gravity.END
                setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            }
            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.alpha = 0.7f
                        true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.alpha = 1f
                        if (event.action == MotionEvent.ACTION_UP) {
                            removeFloatingView()
                            stopSelf()
                        }
                        true
                    }

                    else -> false
                }
            }
        }
        overlayContainer?.addView(closeButton)

        maximizeButton = ImageView(this).apply {
            setImageResource(R.drawable.ic_full_screen)
            setColorFilter(Color.WHITE)

            background = GradientDrawable().apply {
                cornerRadius = dpToPx(16).toFloat()
            }

            layoutParams = FrameLayout.LayoutParams(dpToPx(24), dpToPx(24)).apply {
                gravity = Gravity.TOP or Gravity.START
                setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            }

            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.alpha = 0.7f
                        true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.alpha = 1f
                        if (event.action == MotionEvent.ACTION_UP) {
                            toggleFullScreen()
                        }
                        true
                    }

                    else -> false
                }
            }
        }
        overlayContainer?.addView(maximizeButton)

        val controlBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dpToPx(48) // height of bar
            ).apply {
                gravity = Gravity.BOTTOM
            }
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            setBackgroundColor(Color.parseColor("#80000000")) // optional semi-transparent bg
        }
        playPauseButton = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_media_pause)
            setColorFilter(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#40FFFFFF")) // semi-transparent background
            }
            layoutParams = LinearLayout.LayoutParams(dpToPx(20), dpToPx(20)).apply {

            }

            setOnClickListener {
                videoView?.let { vv ->
                    if (vv.isPlaying) {
                        vv.pause()
                        setImageResource(android.R.drawable.ic_media_play)
                    } else {
                        setImageResource(android.R.drawable.ic_media_pause)
                        vv.start()

                    }
                }
            }
        }

        seekBar = SeekBar(this).apply {
            visibility = View.VISIBLE
            layoutParams = LinearLayout.LayoutParams(
                0, dpToPx(32), 1f // take remaining width
            ).apply {
                setMargins(8,0,8,0)
            }
            setPadding(dpToPx(16), dpToPx(0), dpToPx(16), dpToPx(0))

        }

        controlBar.addView(playPauseButton)
        controlBar.addView(seekBar)

// Add control bar to container
        container.addView(controlBar)
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    videoView?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // pause updates while user is dragging
                progressHandler.removeCallbacksAndMessages(null)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                startProgressUpdater()
                showControls()
            }
        })

        container.addView(overlayContainer)
        return container
    }


    private fun setupVideo(layoutParams: WindowManager.LayoutParams, fixedHeight: Int) {
        currentVideoData?.let { data ->
            val resolvedUri = when (val downloadedState = data.downloadedMedia.value) {
                is AsyncData.Success -> downloadedState.data.uri
                else -> getVideoUriFromMediaSource(data.mediaSource)
            }
            videoView?.apply {
                stopPlayback()
                setVideoURI(resolvedUri)
                setMediaController(null)

                setOnPreparedListener { mediaPlayer ->
                    seekBar?.max = mediaPlayer.duration
                    startProgressUpdater()
                    mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)

                    // Calculate scaled width for fixed height
                    val videoWidth = mediaPlayer.videoWidth
                    val videoHeight = mediaPlayer.videoHeight
                    if (videoWidth > 0 && videoHeight > 0) {
                        val scaledWidth = fixedHeight * videoWidth / videoHeight
                        layoutParams.width = scaledWidth
                        layoutParams.height = fixedHeight
                        windowManager?.updateViewLayout(floatingView, layoutParams)

                        val videoLayoutParams = videoView?.layoutParams
                        videoLayoutParams?.width = scaledWidth
                        videoLayoutParams?.height = fixedHeight
                        videoView?.layoutParams = videoLayoutParams

                        Timber.tag("FloatingVideoService")
                            .d("Updated floating view size: ${scaledWidth}x$fixedHeight")
                    }

                    if (currentPosition > 0) {
                        seekTo(currentPosition.toInt())

                    }
                    start()
                }
                setOnErrorListener { _, what, extra ->
                    showErrorInFloatingView(context.getString(R.string.video_playback_error))
                    true
                }
                setOnInfoListener { _, what, _ ->
                    false
                }
            }
        }
    }


    private fun showErrorInFloatingView(message: String) {
        floatingView?.let { view ->
            // Find or create a TextView to show error
            var errorText = view.findViewWithTag<TextView>("error_text")
            if (errorText == null) {
                errorText = TextView(this).apply {
                    tag = "error_text"
                    text = message
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setBackgroundColor(Color.TRANSPARENT)

                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                }
                (view as ViewGroup).addView(errorText)
            } else {
                errorText.text = message
                errorText.visibility = View.VISIBLE
            }
        }
    }

    private fun setupTouchListener(layoutParams: WindowManager.LayoutParams) {
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragging = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - initialTouchX
                        val deltaY = event.rawY - initialTouchY

                        if (abs(deltaX) > 10 || abs(deltaY) > 10) {
                            isDragging = true
                            layoutParams.x = initialX + deltaX.toInt()
                            layoutParams.y = initialY + deltaY.toInt()

                            layoutParams.x =
                                layoutParams.x.coerceIn(0, getScreenWidth() - dpToPx(150))
                            layoutParams.y =
                                layoutParams.y.coerceIn(0, getScreenHeight() - dpToPx(100))

                            windowManager?.updateViewLayout(floatingView, layoutParams)
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            // Single tap - toggle play/pause
                            videoView?.let { vv ->
                                if (vv.isPlaying) {
                                    vv.pause()
                                    playPauseButton?.setImageResource(
                                        android.R.drawable.ic_media_play
                                    )
                                } else {
                                    vv.start()
                                    playPauseButton?.setImageResource(
                                        android.R.drawable.ic_media_pause
                                    )
                                }
                            }
                        }
                        return true
                    }
                }
                return false
            }
        })
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

    private fun updateButtonSizes(isMaximized: Boolean) {
        val size = if (isMaximized) dpToPx(28) else dpToPx(24)
        val margin = if (isMaximized) dpToPx(12) else dpToPx(4)

        closeButton?.layoutParams =
            (closeButton?.layoutParams as? FrameLayout.LayoutParams)?.apply {
                width = size
                height = size
                setMargins(margin, margin, margin, margin)
            }

        maximizeButton?.layoutParams =
            (maximizeButton?.layoutParams as? FrameLayout.LayoutParams)?.apply {
                width = size
                height = size
                setMargins(margin, margin, margin, margin)
            }


        // Overlay container does not need weird expressions
        overlayContainer?.layoutParams =
            (overlayContainer?.layoutParams as? FrameLayout.LayoutParams)?.apply {
                width = FrameLayout.LayoutParams.MATCH_PARENT
                height = FrameLayout.LayoutParams.WRAP_CONTENT
            }

        // Make sure to request layout after changes
        closeButton?.requestLayout()
        maximizeButton?.requestLayout()
        overlayContainer?.requestLayout()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingView()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun toggleFullScreen() {
        if (!isMaximized) {
            isMaximized = true
            maximizeButton?.apply {
                setImageResource(R.drawable.ic_full_screen)
            }
            updateButtonSizes(false)
            hideControls()

        } else {
            isMaximized = false
            maximizeButton?.apply {
                setImageResource(R.drawable.ic_full_screen_exit)
            }
            updateButtonSizes(true)
            showControls()
        }

        if (floatingView?.parent == null) return

        if (!isMaximized) {
            // Full screen with margin
            val margin = dpToPx(24)

            // shrink the container size by margins
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.x = 0
            layoutParams.y = 0

            // video fills the container, container itself is inset by margins
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels

            layoutParams.width = screenWidth - margin * 2
            layoutParams.height = screenHeight - margin * 2
            layoutParams.x = margin
            layoutParams.y = margin

            videoView?.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )

        } else {
            // Back to floating size
            val fixedHeight = dpToPx(200)
            val scaledWidth = fixedHeight * (videoView?.width ?: 16) / (videoView?.height ?: 9)

            layoutParams.width = scaledWidth
            layoutParams.height = fixedHeight
            layoutParams.x = 0
            layoutParams.y = 0

            videoView?.layoutParams = FrameLayout.LayoutParams(scaledWidth, fixedHeight)
        }

        windowManager?.updateViewLayout(floatingView, layoutParams)
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


    private fun showControls() {
        seekBar?.visibility = View.VISIBLE

        overlayHandler.removeCallbacksAndMessages(null)
        overlayHandler.postDelayed({ hideControls() }, 3000)
        controlsVisibility = true
    }

    private fun hideControls() {

        controlsVisibility = false

    }

    private fun startProgressUpdater() {
        progressHandler.post(object : Runnable {
            override fun run() {
                if (videoView != null && videoView!!.isPlaying) {
                    seekBar?.progress = videoView!!.currentPosition
                    if (!videoView!!.isPlaying) {
                        // show controls automatically when paused
                        showControls()
                    }
                }
                progressHandler.postDelayed(this, 500) // update every 0.5s
            }
        })
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
                Uri.parse(url)
            }
            url.startsWith("file://") -> {
                // Already a file URI
                Uri.parse(url)
            }
            url.startsWith("/") -> {
                // Local file path, convert to file URI
                Uri.fromFile(File(url))
            }
            url.startsWith("content://") -> {
                // Content URI (from MediaStore, etc.)
                Uri.parse(url)
            }
            else -> {
                Log.w("VideoPlayer", "Unknown URL format: $url")
                // Try parsing as-is, might work
                Uri.parse(url)
            }
        }
    } catch (e: Exception) {
        Uri.EMPTY
    }
}
