/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import android.graphics.Bitmap
import io.element.android.libraries.designsystem.components.avatar.AvatarData

/**
 * Generates a bitmap for an initials avatar based on the provided [io.element.android.libraries.designsystem.components.avatar.AvatarData].
 */
interface InitialsAvatarBitmapGenerator {
    /**
     * Draws the initials avatar, used where a bitmap is required rather than a Composable, such as in notifications and shortcuts.
     *
     * @param size the width and height of the bitmap in pixels.
     * @param avatarData the name and id the initials and the background colour are derived from.
     * @param useDarkTheme whether to draw the dark theme variant.
     * @param fontSizePercentage the text height as a fraction of [size].
     * @return the bitmap, or `null` when it could not be drawn.
     */
    fun generateBitmap(
        size: Int,
        avatarData: AvatarData,
        useDarkTheme: Boolean,
        fontSizePercentage: Float = 0.5f,
    ): Bitmap?
}
