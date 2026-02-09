/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.diagnostics

import dev.zacsweers.metro.Inject

@Inject
class BugReportFormatter {
    fun format(diagnostics: Diagnostics): String {
        return buildString {
            appendLine("Summary:")
            appendLine("Steps to reproduce:")
            appendLine("Actual result:")
            appendLine("Expected result:")
            appendLine("Additional notes:")
            appendLine()
            appendLine("--- Diagnostics ---")
            diagnostics.entries
                .toSortedMap()
                .forEach { (key, value) ->
                    appendLine("$key: $value")
                }
        }
    }
}
