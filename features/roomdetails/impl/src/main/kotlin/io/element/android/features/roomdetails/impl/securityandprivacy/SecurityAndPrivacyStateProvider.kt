/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import java.util.Optional

open class SecurityAndPrivacyStateProvider : PreviewParameterProvider<SecurityAndPrivacyState> {
    override val values: Sequence<SecurityAndPrivacyState>
        get() = sequenceOf(
            aSecurityAndPrivacyState(),
            aSecurityAndPrivacyState(
                currentSettings = aSecurityAndPrivacySettings(
                    roomAccess = SecurityAndPrivacyRoomAccess.AskToJoin
                )
            ),
            aSecurityAndPrivacyState(
                currentSettings = aSecurityAndPrivacySettings(
                    roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
                    isEncrypted = false,
                )
            ),
            aSecurityAndPrivacyState(
                currentSettings = aSecurityAndPrivacySettings(
                    roomAccess = SecurityAndPrivacyRoomAccess.SpaceMember
                )
            ),
            aSecurityAndPrivacyState(
                currentSettings = aSecurityAndPrivacySettings(
                    isVisibleInRoomDirectory = Optional.of(AsyncData.Loading())
                )
            ),
            aSecurityAndPrivacyState(
                currentSettings = aSecurityAndPrivacySettings(
                    isVisibleInRoomDirectory = Optional.of(AsyncData.Success(true))
                )
            ),
            aSecurityAndPrivacyState(canBeSaved = false)
        )
}

fun aSecurityAndPrivacySettings(
    roomAccess: SecurityAndPrivacyRoomAccess = SecurityAndPrivacyRoomAccess.InviteOnly,
    isEncrypted: Boolean = true,
    formattedAddress: Optional<String> = Optional.empty(),
    historyVisibility: Optional<SecurityAndPrivacyHistoryVisibility> = Optional.of(SecurityAndPrivacyHistoryVisibility.SinceSelection),
    isVisibleInRoomDirectory: Optional<AsyncData<Boolean>> = Optional.empty()
) = SecurityAndPrivacySettings(
    roomAccess = roomAccess,
    isEncrypted = isEncrypted,
    formattedAddress = formattedAddress,
    historyVisibility = historyVisibility,
    isVisibleInRoomDirectory = isVisibleInRoomDirectory
)

fun aSecurityAndPrivacyState(
    currentSettings: SecurityAndPrivacySettings = aSecurityAndPrivacySettings(),
    savedSettings: SecurityAndPrivacySettings = currentSettings,
    canBeSaved: Boolean = true,
    homeserverName: String = "myserver.xyz",
    eventSink: (SecurityAndPrivacyEvents) -> Unit = {}
) = SecurityAndPrivacyState(
    currentSettings = currentSettings,
    savedSettings = savedSettings,
    homeserverName = homeserverName,
    canBeSaved = canBeSaved,
    eventSink = eventSink
)
