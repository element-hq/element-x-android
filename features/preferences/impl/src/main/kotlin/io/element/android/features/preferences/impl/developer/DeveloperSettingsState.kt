/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import io.element.android.features.preferences.impl.developer.tracing.LogLevelItem
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.libraries.matrix.api.tracing.TraceLogPack
import kotlinx.collections.immutable.ImmutableList

data class DeveloperSettingsState(
    val features: ImmutableList<FeatureUiModel>,
    val cacheSize: AsyncData<String>,
    val rageshakeState: RageshakePreferencesState,
    val clearCacheAction: AsyncAction<Unit>,
    val customElementCallBaseUrlState: CustomElementCallBaseUrlState,
    val tracingLogLevel: AsyncData<LogLevelItem>,
    val tracingLogPacks: ImmutableList<TraceLogPack>,
    val isEnterpriseBuild: Boolean,
    val showColorPicker: Boolean,
    val eventSink: (DeveloperSettingsEvents) -> Unit
) {
    val showLoader = clearCacheAction is AsyncAction.Loading
}

data class CustomElementCallBaseUrlState(
    val baseUrl: String?,
    val validator: (String?) -> Boolean,
)
