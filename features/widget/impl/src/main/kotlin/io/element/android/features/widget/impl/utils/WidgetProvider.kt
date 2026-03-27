/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.impl.utils

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver

interface WidgetProvider {
    suspend fun getWidget(
        sessionId: SessionId,
        roomId: RoomId,
        clientId: String,
        url: String,
        initAfterContentLoad: Boolean,
        languageTag: String?,
        theme: String?,
    ): Result<GetWidgetResult>

    data class GetWidgetResult(
        val driver: MatrixWidgetDriver,
        val url: String,
    )
}

