/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.appconfig.RageshakeConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

@ContributesBinding(AppScope::class)
class DefaultLogFolderProvider(
    private val enterpriseService: EnterpriseService,
    private val sessionStore: SessionStore,
) : LogFolderProvider {
    private val defaultBugReportUrl = RageshakeConfig.BUG_REPORT_URL.toHttpUrlOrNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun logFolderFlow(): Flow<LogFolder> {
        return sessionStore.sessionsFlow()
            .flatMapLatest { sessions ->
                // When no account is signed in, use the configuration which applies when there is no session.
                val sessionIds = sessions.map { SessionId(it.userId) }.ifEmpty { listOf(null) }
                combine(
                    sessionIds.map { enterpriseService.bugReportHttpUrlFlow(it) }
                ) { urls ->
                    computeLogFolder(urls.toList(), defaultBugReportUrl)
                }
            }
            .distinctUntilChanged()
    }
}
