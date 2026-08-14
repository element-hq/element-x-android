/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.widget

import java.util.UUID

/**
 * Builds the widget settings used to embed Element Call in a room, applying the app's own preferences on top of the SDK defaults.
 */
interface CallWidgetSettingsProvider {
    /**
     * @param baseUrl the URL Element Call is served from.
     * @param widgetId a unique identifier for this widget instance.
     * @param encrypted whether the room the call takes place in is encrypted.
     * @param direct whether the call happens in a direct message room.
     * @param isAudioCall whether the call should start without video.
     * @param hasActiveCall whether a call is already ongoing in the room, which affects whether the user joins straight away.
     */
    suspend fun provide(
        baseUrl: String,
        widgetId: String = UUID.randomUUID().toString(),
        encrypted: Boolean,
        direct: Boolean,
        isAudioCall: Boolean,
        hasActiveCall: Boolean,
    ): MatrixWidgetSettings
}
