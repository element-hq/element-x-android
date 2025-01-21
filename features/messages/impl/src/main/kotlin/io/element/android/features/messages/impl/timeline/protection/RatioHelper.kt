/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

fun coerceRatioWhenHidingContent(aspectRatio: Float?, hideContent: Boolean): Float? {
    return if (hideContent) {
        aspectRatio?.coerceIn(
            minimumValue = 0.5f,
            maximumValue = 3f
        )
    } else {
        aspectRatio
    }
}
