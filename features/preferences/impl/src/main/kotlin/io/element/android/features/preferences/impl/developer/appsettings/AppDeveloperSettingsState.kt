/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.appsettings

import io.element.android.features.preferences.impl.developer.tracing.LogLevelItem
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.libraries.matrix.api.tracing.TraceLogPack
import kotlinx.collections.immutable.ImmutableList

data class AppDeveloperSettingsState(
    val features: ImmutableList<FeatureUiModel>,
    val rageshakeState: RageshakePreferencesState,
    val customElementCallBaseUrlState: CustomElementCallBaseUrlState,
    val tracingLogLevel: AsyncData<LogLevelItem>,
    val tracingLogPacks: ImmutableList<TraceLogPack>,
    val eventSink: (AppDeveloperSettingsEvent) -> Unit
)

data class CustomElementCallBaseUrlState(
    val baseUrl: String?,
    val validator: (String?) -> Boolean,
)
