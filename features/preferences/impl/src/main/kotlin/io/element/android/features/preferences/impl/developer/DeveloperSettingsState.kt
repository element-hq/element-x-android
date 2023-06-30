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

import io.element.android.features.rageshake.api.preferences.RageshakePreferencesState
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import kotlinx.collections.immutable.ImmutableList

data class DeveloperSettingsState constructor(
    val features: ImmutableList<FeatureUiModel>,
    val cacheSize: Async<String>,
    val rageshakeState: RageshakePreferencesState,
    val clearCacheAction: Async<Unit>,
    val eventSink: (DeveloperSettingsEvents) -> Unit
)
