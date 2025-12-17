/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.utils

import androidx.compose.ui.graphics.Color

/**
 * Convert color to Human Readable Format.
 */
fun Color.toHrf(): String {
    return "0x" + value.toString(16).take(8).uppercase()
}
