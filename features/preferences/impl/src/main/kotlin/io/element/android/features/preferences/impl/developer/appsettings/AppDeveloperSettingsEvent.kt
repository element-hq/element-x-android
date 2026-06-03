/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.appsettings

import io.element.android.features.preferences.impl.developer.tracing.LogLevelItem
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.libraries.matrix.api.tracing.TraceLogPack

sealed interface AppDeveloperSettingsEvent {
    data class UpdateEnabledFeature(val feature: FeatureUiModel, val isEnabled: Boolean) : AppDeveloperSettingsEvent
    data class SetCustomElementCallBaseUrl(val baseUrl: String?) : AppDeveloperSettingsEvent
    data class SetTracingLogLevel(val logLevel: LogLevelItem) : AppDeveloperSettingsEvent
    data class ToggleTracingLogPack(val logPack: TraceLogPack, val enabled: Boolean) : AppDeveloperSettingsEvent
}
