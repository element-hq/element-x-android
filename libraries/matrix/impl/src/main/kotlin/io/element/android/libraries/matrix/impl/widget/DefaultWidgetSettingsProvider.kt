/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.widget

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.matrix.api.widget.CallAnalyticCredentialsProvider
import io.element.android.libraries.matrix.api.widget.WidgetSettingsProvider
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.flow.first
import org.matrix.rustcomponents.sdk.newVirtualElementCallWidget
import timber.log.Timber
import uniffi.matrix_sdk.EncryptionSystem
import uniffi.matrix_sdk.VirtualElementCallWidgetConfig
import uniffi.matrix_sdk.VirtualElementCallWidgetProperties
import uniffi.matrix_sdk.Intent as CallIntent

@ContributesBinding(AppScope::class)
class IDefaultWidgetSettingsProvider() : WidgetSettingsProvider {
    override suspend fun provide(
        rawUrl: String,
        initAfterContentLoad: Boolean,
        widgetId: String,
    ): MatrixWidgetSettings {

        return MatrixWidgetSettings(
            id = widgetId,
            initAfterContentLoad = initAfterContentLoad,
            rawUrl = rawUrl+"&parentUrl=https%3A%2F%2Fmatrix-expenses-widget-nightly.netlify.app",
        )

    }
}
