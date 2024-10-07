/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.tracing

import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.Target

sealed interface ConfigureTracingEvents {
    data class UpdateFilter(val target: Target, val logLevel: LogLevel) : ConfigureTracingEvents
    data object ResetFilters : ConfigureTracingEvents
}
