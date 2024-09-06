/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.tracing

import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.Target
import io.element.android.libraries.matrix.api.tracing.TracingFilterConfiguration
import javax.inject.Inject

class TargetLogLevelMapBuilder @Inject constructor(
    private val tracingConfigurationStore: TracingConfigurationStore,
    private val defaultConfig: TracingFilterConfiguration,
) {
    fun getDefaultMap(): Map<Target, LogLevel> {
        return Target.entries.associateWith { target ->
            defaultConfig.getLogLevel(target)
        }
    }

    fun getCurrentMap(): Map<Target, LogLevel> {
        return Target.entries.associateWith { target ->
            tracingConfigurationStore.getLogLevel(target)
                ?: defaultConfig.getLogLevel(target)
        }
    }
}
