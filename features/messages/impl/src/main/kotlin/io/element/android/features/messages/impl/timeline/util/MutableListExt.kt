/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.util

internal inline fun <reified T> MutableList<T?>.invalidateLast() {
    val indexOfLast = size
    if (indexOfLast > 0) {
        set(indexOfLast - 1, null)
    }
}
