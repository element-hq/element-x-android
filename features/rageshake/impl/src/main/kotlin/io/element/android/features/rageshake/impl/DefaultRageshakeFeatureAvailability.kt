/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.rageshake.api.RageshakeFeatureAvailability
import io.element.android.features.rageshake.impl.reporter.BugReporterUrlProvider
import dev.zacsweers.metro.AppScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class DefaultRageshakeFeatureAvailability(
    private val bugReporterUrlProvider: BugReporterUrlProvider,
) : RageshakeFeatureAvailability {
    override fun isAvailable(): Flow<Boolean> {
        return bugReporterUrlProvider.provide()
            .map { it != null }
    }
}
