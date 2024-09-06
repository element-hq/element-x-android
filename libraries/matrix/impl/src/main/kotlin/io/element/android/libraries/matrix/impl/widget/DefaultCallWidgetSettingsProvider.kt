/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.widget

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.widget.CallWidgetSettingsProvider
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import org.matrix.rustcomponents.sdk.EncryptionSystem
import org.matrix.rustcomponents.sdk.VirtualElementCallWidgetOptions
import org.matrix.rustcomponents.sdk.newVirtualElementCallWidget
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultCallWidgetSettingsProvider @Inject constructor() : CallWidgetSettingsProvider {
    override fun provide(baseUrl: String, widgetId: String, encrypted: Boolean): MatrixWidgetSettings {
        val options = VirtualElementCallWidgetOptions(
            elementCallUrl = baseUrl,
            widgetId = widgetId,
            parentUrl = null,
            hideHeader = null,
            preload = null,
            fontScale = null,
            appPrompt = false,
            skipLobby = true,
            confineToRoom = true,
            font = null,
            analyticsId = null,
            encryption = if (encrypted) EncryptionSystem.PerParticipantKeys else EncryptionSystem.Unencrypted,
        )
        val rustWidgetSettings = newVirtualElementCallWidget(options)
        return MatrixWidgetSettings.fromRustWidgetSettings(rustWidgetSettings)
    }
}
