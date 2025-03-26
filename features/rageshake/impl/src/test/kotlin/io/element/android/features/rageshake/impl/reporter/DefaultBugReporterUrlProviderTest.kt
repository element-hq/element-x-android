/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.RageshakeConfig
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Test

class DefaultBugReporterUrlProviderTest {
    @Test
    fun `test DefaultBugReporterUrlProvider`() {
        val sut = DefaultBugReporterUrlProvider()
        if (RageshakeConfig.BUG_REPORT_URL.isNotEmpty()) {
            val result = sut.provide()
            assertThat(result).isEqualTo(RageshakeConfig.BUG_REPORT_URL.toHttpUrl())
        }
    }
}
