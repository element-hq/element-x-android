/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.media

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.util.Rational

/**
 * Set the aspect ratio of the Picture-in-Picture mode based on the current orientation of the other user's device.
 */
fun PictureInPictureParams.Builder.setAspectRatioFromOrientation(orientation: Int): PictureInPictureParams.Builder {
    val aspectRatio = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        // If landscape orientation, invert the aspect ratio
        Rational(5, 3)
    } else {
        // In any other case, assume portrait orientation
        Rational(3, 5)
    }
    return setAspectRatio(aspectRatio)
}
