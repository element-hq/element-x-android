/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
        val baseUrl = "https://appassets.androidplatform.net/index.html"
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
