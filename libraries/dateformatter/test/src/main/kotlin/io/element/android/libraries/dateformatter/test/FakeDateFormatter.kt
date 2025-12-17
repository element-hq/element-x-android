/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.test

import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode

class FakeDateFormatter(
    private val formatLambda: (Long?, DateFormatterMode, Boolean) -> String = { timestamp, mode, useRelative ->
        "$timestamp $mode $useRelative"
    },
) : DateFormatter {
    override fun format(
        timestamp: Long?,
        mode: DateFormatterMode,
        useRelative: Boolean,
    ): String {
        return formatLambda(timestamp, mode, useRelative)
    }
}
