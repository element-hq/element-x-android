/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.appconfig.RageshakeConfig
import io.element.android.features.enterprise.api.BugReportUrl
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.api.sessionIdFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

@ContributesBinding(AppScope::class)
class DefaultBugReporterUrlProvider(
    private val bugReportAppNameProvider: BugReportAppNameProvider,
    private val enterpriseService: EnterpriseService,
    private val sessionStore: SessionStore,
) : BugReporterUrlProvider {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun provide(): Flow<HttpUrl?> {
        if (bugReportAppNameProvider.provide().isEmpty()) return flowOf(null)
        return sessionStore.sessionIdFlow().flatMapLatest { sessionId ->
            enterpriseService.bugReportUrlFlow(sessionId?.let(::SessionId))
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
}
