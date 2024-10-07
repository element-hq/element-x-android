/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import io.element.android.features.rageshake.api.preferences.RageshakePreferencesState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import kotlinx.collections.immutable.ImmutableList

data class DeveloperSettingsState(
    val features: ImmutableList<FeatureUiModel>,
    val cacheSize: AsyncData<String>,
    val rageshakeState: RageshakePreferencesState,
    val clearCacheAction: AsyncData<Unit>,
    val customElementCallBaseUrlState: CustomElementCallBaseUrlState,
    val isSimpleSlidingSyncEnabled: Boolean,
    val hideImagesAndVideos: Boolean,
    val eventSink: (DeveloperSettingsEvents) -> Unit
)

data class CustomElementCallBaseUrlState(
    val baseUrl: String?,
    val defaultUrl: String,
    val validator: (String?) -> Boolean,
)
