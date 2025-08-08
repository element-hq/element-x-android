/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.RageshakeConfig
import io.element.android.features.enterprise.api.BugReportUrl
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.di.AppScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultBugReporterUrlProvider @Inject constructor(
    private val bugReportAppNameProvider: BugReportAppNameProvider,
    private val enterpriseService: EnterpriseService,
) : BugReporterUrlProvider {
    override fun provide(): Flow<HttpUrl?> {
        if (bugReportAppNameProvider.provide().isEmpty()) return flowOf(null)
        return enterpriseService.bugReportUrlFlow
            .map { bugReportUrl ->
                when (bugReportUrl) {
                    is BugReportUrl.Custom -> bugReportUrl.url
                    BugReportUrl.Disabled -> null
                    BugReportUrl.UseDefault -> RageshakeConfig.BUG_REPORT_URL.takeIf { it.isNotEmpty() }
                }
            }
            .map { it?.toHttpUrl() }
    }
}
