/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.call.utils

import io.element.android.features.call.impl.utils.CallWidgetProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.test.widget.FakeMatrixWidgetDriver

class FakeCallWidgetProvider(
    private val widgetDriver: FakeMatrixWidgetDriver = FakeMatrixWidgetDriver(),
    private val url: String = "https://call.element.io",
) : CallWidgetProvider {
    var getWidgetCalled = false
        private set

    override suspend fun getWidget(
        sessionId: SessionId,
        roomId: RoomId,
        clientId: String,
        languageTag: String?,
        theme: String?
    ): Result<Pair<MatrixWidgetDriver, String>> {
        getWidgetCalled = true
        return Result.success(widgetDriver to url)
    }
}
