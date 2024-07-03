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

package io.element.android.features.call.impl.utils

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.ElementCallConfig
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.call.ElementCallBaseUrlProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.widget.CallWidgetSettingsProvider
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultCallWidgetProvider @Inject constructor(
    private val matrixClientsProvider: MatrixClientProvider,
    private val appPreferencesStore: AppPreferencesStore,
    private val callWidgetSettingsProvider: CallWidgetSettingsProvider,
    private val elementCallBaseUrlProvider: ElementCallBaseUrlProvider,
) : CallWidgetProvider {
    override suspend fun getWidget(
        sessionId: SessionId,
        roomId: RoomId,
        clientId: String,
        languageTag: String?,
        theme: String?,
    ): Result<CallWidgetProvider.GetWidgetResult> = runCatching {
        val matrixClient = matrixClientsProvider.getOrRestore(sessionId).getOrThrow()
        val room = matrixClient.getRoom(roomId) ?: error("Room not found")
        val baseUrl = appPreferencesStore.getCustomElementCallBaseUrlFlow().firstOrNull()
            ?: elementCallBaseUrlProvider.provides(matrixClient)
            ?: ElementCallConfig.DEFAULT_BASE_URL
        val widgetSettings = callWidgetSettingsProvider.provide(baseUrl, encrypted = room.isEncrypted)
        val callUrl = room.generateWidgetWebViewUrl(
            widgetSettings = widgetSettings,
            clientId = clientId,
            languageTag = languageTag,
            theme = theme,
        ).getOrThrow()
        CallWidgetProvider.GetWidgetResult(
            driver = room.getWidgetDriver(widgetSettings).getOrThrow(),
            url = callUrl
        )
    }
}
