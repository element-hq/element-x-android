/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.diagnostics

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BugReportFormatterTest {
    @Test
    fun `format - includes template and diagnostics in stable order`() {
        val diagnostics = Diagnostics(
            entries = mapOf(
                "Zebra" to "last",
                "Alpha" to "first",
            ),
        )

        val report = BugReportFormatter().format(diagnostics)

        assertThat(report).contains("Summary:")
        assertThat(report).contains("Steps to reproduce:")
        assertThat(report).contains("Actual result:")
        assertThat(report).contains("Expected result:")
        assertThat(report).contains("Additional notes:")
        assertThat(report).contains("--- Diagnostics ---")
        val alphaIndex = report.indexOf("Alpha: first")
        val zebraIndex = report.indexOf("Zebra: last")
        assertThat(alphaIndex).isAtLeast(0)
        assertThat(zebraIndex).isAtLeast(0)
        assertThat(alphaIndex).isLessThan(zebraIndex)
    }
}
