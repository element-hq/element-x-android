/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.appsettings

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.preferences.impl.developer.tracing.LogLevelItem
import io.element.android.features.rageshake.api.preferences.aRageshakePreferencesState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.featureflag.ui.model.aFeatureUiModelList
import io.element.android.libraries.matrix.api.tracing.TraceLogPack
import kotlinx.collections.immutable.toImmutableList

open class AppDeveloperSettingsStateProvider : PreviewParameterProvider<AppDeveloperSettingsState> {
    override val values: Sequence<AppDeveloperSettingsState>
        get() = sequenceOf(
            anAppDeveloperSettingsState(),
            anAppDeveloperSettingsState(
                customElementCallBaseUrlState = aCustomElementCallBaseUrlState(
                    baseUrl = "https://call.element.ahoy",
                )
            ),
        )
}

fun anAppDeveloperSettingsState(
    customElementCallBaseUrlState: CustomElementCallBaseUrlState = aCustomElementCallBaseUrlState(),
    traceLogPacks: List<TraceLogPack> = emptyList(),
    eventSink: (AppDeveloperSettingsEvent) -> Unit = {},
) = AppDeveloperSettingsState(
    features = aFeatureUiModelList(),
    rageshakeState = aRageshakePreferencesState(),
    customElementCallBaseUrlState = customElementCallBaseUrlState,
    tracingLogLevel = AsyncData.Success(LogLevelItem.INFO),
    tracingLogPacks = traceLogPacks.toImmutableList(),
    eventSink = eventSink,
)

fun aCustomElementCallBaseUrlState(
    baseUrl: String? = null,
    validator: (String?) -> Boolean = { true },
) = CustomElementCallBaseUrlState(
    baseUrl = baseUrl,
    validator = validator,
)
