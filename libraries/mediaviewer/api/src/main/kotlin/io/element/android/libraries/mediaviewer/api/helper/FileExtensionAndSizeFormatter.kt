/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.helper

fun formatFileExtensionAndSize(extension: String, size: String?): String {
    return buildString {
        append(extension.uppercase())
        if (size != null) {
            append(' ')
            append("($size)")
        }
    }
}
