/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.preferences.impl.developer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.rageshake.api.preferences.aRageshakePreferencesState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.featureflag.ui.model.aFeatureUiModelList

open class DeveloperSettingsStateProvider : PreviewParameterProvider<DeveloperSettingsState> {
    override val values: Sequence<DeveloperSettingsState>
        get() = sequenceOf(
            aDeveloperSettingsState(),
            aDeveloperSettingsState(
                clearCacheAction = AsyncData.Loading()
            ),
            aDeveloperSettingsState(
                customElementCallBaseUrlState = aCustomElementCallBaseUrlState(
                    baseUrl = "https://call.element.ahoy",
                )
            ),
        )
}

fun aDeveloperSettingsState(
    clearCacheAction: AsyncData<Unit> = AsyncData.Uninitialized,
    customElementCallBaseUrlState: CustomElementCallBaseUrlState = aCustomElementCallBaseUrlState(),
    customSlidingSyncProxy: String? = null,
    eventSink: (DeveloperSettingsEvents) -> Unit = {},
) = DeveloperSettingsState(
    features = aFeatureUiModelList(),
    rageshakeState = aRageshakePreferencesState(),
    cacheSize = AsyncData.Success("1.2 MB"),
    clearCacheAction = clearCacheAction,
    customElementCallBaseUrlState = customElementCallBaseUrlState,
    customSlidingSyncProxy = customSlidingSyncProxy,
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
