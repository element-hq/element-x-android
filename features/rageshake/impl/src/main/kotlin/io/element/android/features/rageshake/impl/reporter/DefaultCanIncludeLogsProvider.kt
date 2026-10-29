/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@ContributesBinding(AppScope::class)
class DefaultCanIncludeLogsProvider(
    private val logFolderProvider: LogFolderProvider,
) : CanIncludeLogsProvider {
    override fun canIncludeLogs(): Flow<Boolean> {
        return logFolderProvider.logFolderFlow()
            .map { it != LogFolder.LocalOnly }
            .distinctUntilChanged()
    }
}
