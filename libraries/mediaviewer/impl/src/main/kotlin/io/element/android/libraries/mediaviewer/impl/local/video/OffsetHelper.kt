/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.video

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import timber.log.Timber
import kotlin.math.abs

data class OffsetHelper(
    val videoSize: IntSize?,
    val playerSize: IntSize?,
) {
    private val screenRatio: Float? = playerSize?.takeIf { it.height > 0 }?.let {
        it.width.toFloat() / it.height.toFloat()
    }
    private val videoRatio: Float? = videoSize?.takeIf { it.height > 0 }?.let {
        it.width.toFloat() / it.height.toFloat()
    }

    fun computeOffset(
        scale: Float,
        newOffset: Offset,
    ): Offset {
        // Safety guards
        playerSize ?: return Offset.Zero
        screenRatio ?: return Offset.Zero
        videoRatio ?: return Offset.Zero
        return if (screenRatio <= videoRatio) {
            // For instance, phone in portrait mode and video as a regular movie
            // Size of the video with scale == 1
            val actualVideoSize = IntSize(
                width = playerSize.width,
                height = (playerSize.width / videoRatio).toInt(),
            )
            val translationX = if (actualVideoSize.width * scale > playerSize.width) {
                // Result should be positive but enforce this
                val xLimit = abs(playerSize.width / 2 * (scale - 1))
                newOffset.x.coerceIn(-xLimit, xLimit)
            } else {
                // Video width is smaller than the screen width, do not allow X translation
                0f
            }
            val translationY = if (actualVideoSize.height * scale > playerSize.height) {
                // Video height is larger than the screen height, allow Y translation
                Timber.e("Video size: $actualVideoSize, Player size: $playerSize, Scale: $scale")
                // Result should be positive but enforce this
                val yLimit = abs((actualVideoSize.height * scale - playerSize.height) / 2)
                newOffset.y.coerceIn(-yLimit, yLimit).also {
                    Timber.e("Video size: $actualVideoSize, Player size: $playerSize, Scale: $scale, Y Limit: $yLimit, New Offset Y: ${newOffset.y}, Translation Y: $it")
                }
            } else {
                // Video height is smaller than the screen height, do not allow Y translation
                0f
            }
            Offset(translationX, translationY)
        } else {
            // For instance, phone in landscape mode and video as a vertical video
            // Size of the video with scale == 1
            val actualVideoSize = IntSize(
                width = (playerSize.height * videoRatio).toInt(),
                height = playerSize.height,
            )
            val translationX = if (actualVideoSize.width * scale > playerSize.width) {
                val xLimit = (actualVideoSize.width * scale - playerSize.width) / 2
                newOffset.x.coerceIn(-xLimit, xLimit)
            } else {
                // Video width is smaller than the screen width, do not allow X translation
                0f
            }
            val translationY = if (actualVideoSize.height * scale > playerSize.height) {
                // Video height is larger than the screen height, allow Y translation
                val yLimit = playerSize.height / 2 * (scale - 1)
                newOffset.y.coerceIn(-yLimit, yLimit)
            } else {
                // Video height is smaller than the screen height, do not allow Y translation
                0f
            }
            Offset(translationX, translationY)
        }
    }
}
