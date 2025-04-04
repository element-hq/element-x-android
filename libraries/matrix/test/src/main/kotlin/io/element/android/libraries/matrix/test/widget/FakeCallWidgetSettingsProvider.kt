/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.widget

import io.element.android.libraries.matrix.api.widget.CallWidgetSettingsProvider
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings

class FakeCallWidgetSettingsProvider(
    private val provideFn: (String, String) -> MatrixWidgetSettings = { _, _ -> MatrixWidgetSettings("id", true, "url") }
) : CallWidgetSettingsProvider {
    val providedBaseUrls = mutableListOf<String>()

    override suspend fun provide(baseUrl: String, widgetId: String, encrypted: Boolean): MatrixWidgetSettings {
        providedBaseUrls += baseUrl
        return provideFn(baseUrl, widgetId)
    }
}
