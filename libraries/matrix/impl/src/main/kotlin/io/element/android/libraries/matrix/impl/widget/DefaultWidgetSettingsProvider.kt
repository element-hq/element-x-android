/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.widget

import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.widget.WidgetSettingsProvider
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings

@ContributesBinding(AppScope::class)
class IDefaultWidgetSettingsProvider() : WidgetSettingsProvider {
    override suspend fun provide(
        rawUrl: String,
        initAfterContentLoad: Boolean,
        widgetId: String,
    ): MatrixWidgetSettings {
        val parsedUri = rawUrl.toUri()
        val parentUrl = parsedUri.buildUpon()
            .clearQuery()
            .fragment(null)
            .build()
            .toString()
        val updatedUrl = parsedUri.buildUpon()
            .appendQueryParameter("parentUrl", parentUrl)
            .build()
            .toString()
        return MatrixWidgetSettings(
            id = widgetId,
            initAfterContentLoad = initAfterContentLoad,
            rawUrl = updatedUrl,
        )

    }
}
