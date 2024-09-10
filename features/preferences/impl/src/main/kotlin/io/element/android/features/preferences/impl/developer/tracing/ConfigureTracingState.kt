/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.tracing

import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.Target
import kotlinx.collections.immutable.ImmutableMap

data class ConfigureTracingState(
    val targetsToLogLevel: ImmutableMap<Target, LogLevel>,
    val eventSink: (ConfigureTracingEvents) -> Unit
)
