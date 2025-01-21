/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.preferences.impl.developer.tracing.LogLevelItem
import io.element.android.features.rageshake.api.preferences.aRageshakePreferencesState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.featureflag.ui.model.aFeatureUiModelList

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
        )
}

fun aDeveloperSettingsState(
    clearCacheAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    customElementCallBaseUrlState: CustomElementCallBaseUrlState = aCustomElementCallBaseUrlState(),
    isSimplifiedSlidingSyncEnabled: Boolean = false,
    hideImagesAndVideos: Boolean = false,
    eventSink: (DeveloperSettingsEvents) -> Unit = {},
) = DeveloperSettingsState(
    features = aFeatureUiModelList(),
    rageshakeState = aRageshakePreferencesState(),
    cacheSize = AsyncData.Success("1.2 MB"),
    clearCacheAction = clearCacheAction,
    customElementCallBaseUrlState = customElementCallBaseUrlState,
    isSimpleSlidingSyncEnabled = isSimplifiedSlidingSyncEnabled,
    hideImagesAndVideos = hideImagesAndVideos,
    tracingLogLevel = AsyncData.Success(LogLevelItem.INFO),
    eventSink = eventSink,
)

fun aCustomElementCallBaseUrlState(
    baseUrl: String? = null,
    defaultUrl: String = "https://call.element.io",
    validator: (String?) -> Boolean = { true },
) = CustomElementCallBaseUrlState(
    baseUrl = baseUrl,
    defaultUrl = defaultUrl,
    validator = validator,
)
