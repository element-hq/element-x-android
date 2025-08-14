/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.RageshakeConfig
import io.element.android.features.enterprise.api.BugReportUrl
import io.element.android.features.enterprise.test.FakeEnterpriseService
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Test

class DefaultBugReporterUrlProviderTest {
    @Test
    fun `provide return values when there is an rageshake app name`() = runTest {
        val enterpriseService = FakeEnterpriseService()
        val sut = DefaultBugReporterUrlProvider(
            bugReportAppNameProvider = { "rageshakeAppName" },
            enterpriseService = enterpriseService,
        )
        sut.provide().test {
            assertThat(awaitItem()).isEqualTo(
                RageshakeConfig.BUG_REPORT_URL.takeIf { it.isNotEmpty() }?.toHttpUrl()
            )
            enterpriseService.bugReportUrlMutableFlow.emit(BugReportUrl.Disabled)
            assertThat(awaitItem()).isNull()
            enterpriseService.bugReportUrlMutableFlow.emit(BugReportUrl.Custom("https://aURL.org"))
            assertThat(awaitItem()).isEqualTo("https://aURL.org".toHttpUrl())
        }
    }

    @Test
    fun `provide return null when there is no rageshake app name`() = runTest {
        val enterpriseService = FakeEnterpriseService()
        val sut = DefaultBugReporterUrlProvider(
            bugReportAppNameProvider = { "" },
            enterpriseService = enterpriseService,
        )
        sut.provide().test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }
}
