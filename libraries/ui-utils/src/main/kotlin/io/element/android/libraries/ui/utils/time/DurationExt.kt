/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.utils.time

import kotlin.time.Duration

/**
 * Format a duration as minutes:seconds.
 *
 * For example,
 * - 0 seconds will be formatted as "0:00".
 * - 65 seconds will be formatted as "1:05".
 * - 2 hours will be formatted as "120:00".
 * - negative 10 seconds will be formatted as "-0:10".
 *
 * @return the formatted duration.
 */
fun Duration.formatShort(): String {
    // Format as minutes:seconds
    val seconds = (absoluteValue.inWholeSeconds % 60)
        .toString()
        .padStart(2, '0')

    val sign = isNegative().let { if (it) "-" else "" }

    return "$sign${absoluteValue.inWholeMinutes}:$seconds"
}
