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

package io.element.android.libraries.matrix.impl.widget

import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import org.matrix.rustcomponents.sdk.ClientProperties
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.WidgetSettings
import org.matrix.rustcomponents.sdk.generateWebviewUrl

fun MatrixWidgetSettings.toRustWidgetSettings() = WidgetSettings(
    widgetId = this.id,
    initAfterContentLoad = this.initAfterContentLoad,
    rawUrl = this.rawUrl,
)

fun MatrixWidgetSettings.Companion.fromRustWidgetSettings(widgetSettings: WidgetSettings) = MatrixWidgetSettings(
    id = widgetSettings.widgetId,
    initAfterContentLoad = widgetSettings.initAfterContentLoad,
    rawUrl = widgetSettings.rawUrl,
)

suspend fun MatrixWidgetSettings.generateWidgetWebViewUrl(
    room: Room,
    clientId: String,
    languageTag: String? = null,
    theme: String? = null
) = generateWebviewUrl(
    widgetSettings = this.toRustWidgetSettings(),
    room = room,
    props = ClientProperties(
        clientId = clientId,
        languageTag = languageTag,
        theme = theme,
    )
)
