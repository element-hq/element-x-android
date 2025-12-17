/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.widget

import java.util.UUID

interface CallWidgetSettingsProvider {
    suspend fun provide(
        baseUrl: String,
        widgetId: String = UUID.randomUUID().toString(),
        encrypted: Boolean,
        direct: Boolean,
        hasActiveCall: Boolean,
    ): MatrixWidgetSettings
}
