/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.preferences.impl.developer.tracing.LogLevelItem
import io.element.android.features.rageshake.api.preferences.aRageshakePreferencesState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.featureflag.ui.model.aFeatureUiModelList
import io.element.android.libraries.matrix.api.tracing.TraceLogPack
import kotlinx.collections.immutable.toImmutableList

open class DeveloperSettingsStateProvider : PreviewParameterProvider<DeveloperSettingsState> {
    override val values: Sequence<DeveloperSettingsState>
        get() = sequenceOf(
            aDeveloperSettingsState(),
            aDeveloperSettingsState(
                clearCacheAction = AsyncAction.Loading
            ),
            aDeveloperSettingsState(
                customElementCallBaseUrlState = aCustomElementCallBaseUrlState(
                    baseUrl = "https://call.element.ahoy",
                )
            ),
            aDeveloperSettingsState(
                isEnterpriseBuild = true,
                showColorPicker = true,
            ),
        )
}

fun aDeveloperSettingsState(
    clearCacheAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    customElementCallBaseUrlState: CustomElementCallBaseUrlState = aCustomElementCallBaseUrlState(),
    traceLogPacks: List<TraceLogPack> = emptyList(),
    isEnterpriseBuild: Boolean = false,
    showColorPicker: Boolean = false,
    eventSink: (DeveloperSettingsEvents) -> Unit = {},
) = DeveloperSettingsState(
    features = aFeatureUiModelList(),
    rageshakeState = aRageshakePreferencesState(),
    cacheSize = AsyncData.Success("1.2 MB"),
    clearCacheAction = clearCacheAction,
    customElementCallBaseUrlState = customElementCallBaseUrlState,
    tracingLogLevel = AsyncData.Success(LogLevelItem.INFO),
    tracingLogPacks = traceLogPacks.toImmutableList(),
    isEnterpriseBuild = isEnterpriseBuild,
    showColorPicker = showColorPicker,
    eventSink = eventSink,
)

fun aCustomElementCallBaseUrlState(
    baseUrl: String? = null,
    validator: (String?) -> Boolean = { true },
) = CustomElementCallBaseUrlState(
    baseUrl = baseUrl,
    validator = validator,
)
