/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import java.util.Optional
import javax.inject.Inject

class SecurityAndPrivacyPresenter @Inject constructor() : Presenter<SecurityAndPrivacyState> {

    @Composable
    override fun present(): SecurityAndPrivacyState {

        val savedSettings by remember {
            mutableStateOf(
                SecurityAndPrivacySettings(
                    roomAccess = SecurityAndPrivacyRoomAccess.InviteOnly,
                    isEncrypted = true,
                    isVisibleInRoomDirectory = Optional.empty(),
                    historyVisibility = Optional.empty(),
                    formattedAddress = Optional.empty(),
                )
            )
        }

        fun handleEvents(event: SecurityAndPrivacyEvents) {
            when (event) {
                SecurityAndPrivacyEvents.Save -> {}
                is SecurityAndPrivacyEvents.ChangeRoomAccess -> {}
                is SecurityAndPrivacyEvents.EnableEncryption -> {}
                is SecurityAndPrivacyEvents.ChangeHistoryVisibility -> {}
                is SecurityAndPrivacyEvents.ChangeVisibleInRoomDirectory -> {}
            }
        }

        return SecurityAndPrivacyState(
            savedSettings = savedSettings,
            currentSettings = savedSettings,
            homeserverName = "",
            canBeSaved = true,
            eventSink = ::handleEvents
        )
    }
}
