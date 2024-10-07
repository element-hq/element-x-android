/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.widget

import java.util.UUID

interface CallWidgetSettingsProvider {
    fun provide(
        baseUrl: String,
        widgetId: String = UUID.randomUUID().toString(),
        encrypted: Boolean,
    ): MatrixWidgetSettings
}
