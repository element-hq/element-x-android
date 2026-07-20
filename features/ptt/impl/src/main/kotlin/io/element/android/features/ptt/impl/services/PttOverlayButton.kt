/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl.services

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import io.element.android.libraries.core.extensions.runCatchingExceptions

/**
 * A floating press-and-hold PTT button drawn over other apps via a [WindowManager] overlay
 * (TYPE_APPLICATION_OVERLAY). Lets the user transmit while the Element app is backgrounded without a
 * Bluetooth accessory. Touch-down takes the floor ([onPressStart]); touch-up/cancel releases it
 * ([onPressEnd]).
 *
 * Requires the SYSTEM_ALERT_WINDOW permission; [show] is a no-op if it isn't granted. Owned by the
 * foreground [PttSessionHostService] for the life of a session.
 */
class PttOverlayButton(
    private val context: Context,
    private val onPressStart: () -> Unit,
    private val onPressEnd: () -> Unit,
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var view: View? = null

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (view != null || !Settings.canDrawOverlays(context)) return
        val density = context.resources.displayMetrics.density
        val button = Button(context).apply {
            text = "PTT"
            setTextColor(Color.WHITE)
            background = ovalDrawable(COLOR_IDLE)
            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        background = ovalDrawable(COLOR_ACTIVE)
                        onPressStart()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        background = ovalDrawable(COLOR_IDLE)
                        onPressEnd()
                        true
                    }
                    else -> false
                }
            }
        }
        val sizePx = (BUTTON_SIZE_DP * density).toInt()
        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            overlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = (MARGIN_DP * density).toInt()
            y = (BOTTOM_MARGIN_DP * density).toInt()
        }
        runCatchingExceptions { windowManager.addView(button, params) }
            .onSuccess { view = button }
    }

    fun hide() {
        view?.let { current -> runCatchingExceptions { windowManager.removeView(current) } }
        view = null
    }

    private fun ovalDrawable(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    private fun overlayWindowType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

    private companion object {
        const val BUTTON_SIZE_DP = 72
        const val MARGIN_DP = 24
        const val BOTTOM_MARGIN_DP = 120
        val COLOR_IDLE = Color.parseColor("#0DBD8B") // Element green
        val COLOR_ACTIVE = Color.parseColor("#FF5B55") // red while transmitting
    }
}
