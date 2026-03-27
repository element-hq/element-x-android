/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.test

import io.element.android.features.widget.impl.utils.CallWidgetProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.widget.FakeMatrixWidgetDriver

class FakeWidgetProvider(
    private val widgetDriver: FakeMatrixWidgetDriver = FakeMatrixWidgetDriver(),
    private val url: String = "https://widget.element.io",
) : CallWidgetProvider {
    var getWidgetCalled = false
        private set

    override suspend fun getWidget(
        sessionId: SessionId,
        roomId: RoomId,
        clientId: String,
        languageTag: String?,
        theme: String?,
    ): Result<CallWidgetProvider.GetWidgetResult> {
        getWidgetCalled = true
        return Result.success(
            CallWidgetProvider.GetWidgetResult(
                driver = widgetDriver,
                url = url,
            )
        )
    }
}

