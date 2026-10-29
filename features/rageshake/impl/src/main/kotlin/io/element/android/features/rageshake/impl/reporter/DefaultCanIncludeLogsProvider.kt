/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class DefaultCanIncludeLogsProvider(
    private val enterpriseService: EnterpriseService,
    private val sessionStore: SessionStore,
) : CanIncludeLogsProvider {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun canIncludeLogs(): Flow<Boolean> {
        return sessionStore.sessionsFlow()
            .flatMapLatest { sessions ->
                if (sessions.size <= 1) {
                    flowOf(true)
                } else {
                    combine(
                        sessions.map { enterpriseService.bugReportHttpUrlFlow(SessionId(it.userId)) }
                    ) { urls ->
                        urls.all { it != null } && urls.distinct().size == 1
                    }
                }
            }
            .distinctUntilChanged()
    }
}
