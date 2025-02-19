/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import io.element.android.features.preferences.impl.developer.tracing.LogLevelItem
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel

sealed interface DeveloperSettingsEvents {
    data class UpdateEnabledFeature(val feature: FeatureUiModel, val isEnabled: Boolean) : DeveloperSettingsEvents
    data class SetCustomElementCallBaseUrl(val baseUrl: String?) : DeveloperSettingsEvents
    data class SetHideImagesAndVideos(val value: Boolean) : DeveloperSettingsEvents
    data class SetTracingLogLevel(val logLevel: LogLevelItem) : DeveloperSettingsEvents
    data object ClearCache : DeveloperSettingsEvents
}
