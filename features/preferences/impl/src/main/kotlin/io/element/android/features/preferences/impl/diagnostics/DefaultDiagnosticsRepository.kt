/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.diagnostics

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultDiagnosticsRepository(
    private val diagnosticsProvider: DiagnosticsProvider,
    private val coroutineDispatchers: CoroutineDispatchers,
) : DiagnosticsRepository {
    override fun getDiagnostics(): Flow<Diagnostics> {
        return flow {
            emit(diagnosticsProvider.getDiagnostics())
        }.flowOn(coroutineDispatchers.io)
    }
}
