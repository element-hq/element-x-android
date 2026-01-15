/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.getDisplayName
import io.element.android.libraries.matrix.api.timeline.item.event.isCritical
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun MessageShieldView(
    shield: MessageShieldData,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            imageVector = shield.toIcon(),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = shield.toIconColor(),
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = shield.toText(),
            style = ElementTheme.typography.fontBodySmMedium,
            color = shield.toTextColor()
        )
    }
}

data class MessageShieldData(
    /**
     * The message shield that the rust layer thinks we should show.
     */
    val shield: MessageShield,
    /**
     * If the keys to this message were forwarded by another user via history sharing (MSC4268), the ID of that user.
     */
    val forwarder: UserId? = null,
    /** If [forwarder] is set, the profile of the forwarding user, if it was cached at the time the `EventTimelineItem` was created. */
    val forwarderProfile: ProfileDetails? = null,
)

val MessageShieldData.isCritical: Boolean
    get() = shield.isCritical

@Composable
internal fun MessageShieldData.toIconColor(): Color {
    return when (isCritical) {
        true -> ElementTheme.colors.iconCriticalPrimary
        false -> ElementTheme.colors.iconSecondary
    }
}

@Composable
private fun MessageShieldData.toTextColor(): Color {
    return when (isCritical) {
        true -> ElementTheme.colors.textCriticalPrimary
        false -> ElementTheme.colors.textSecondary
    }
}

@Composable
internal fun MessageShieldData.toText(): String {
    if (shield is MessageShield.AuthenticityNotGuaranteed && forwarder != null) {
        var displayName = forwarderProfile?.getDisplayName()
        return if (displayName == null) {
            stringResource(
                CommonStrings.crypto_event_key_forwarded_unknown_profile_dialog_content,
                forwarder.toString(),
            )
        } else {
            stringResource(
                CommonStrings.crypto_event_key_forwarded_known_profile_dialog_content,
                displayName,
                forwarder.toString(),
            )
        }
    }
    return stringResource(
        id = when (shield) {
            is MessageShield.AuthenticityNotGuaranteed -> CommonStrings.event_shield_reason_authenticity_not_guaranteed
            is MessageShield.UnknownDevice -> CommonStrings.event_shield_reason_unknown_device
            is MessageShield.UnsignedDevice -> CommonStrings.event_shield_reason_unsigned_device
            is MessageShield.UnverifiedIdentity -> CommonStrings.event_shield_reason_unverified_identity
            is MessageShield.SentInClear -> CommonStrings.event_shield_reason_sent_in_clear
            is MessageShield.VerificationViolation -> CommonStrings.event_shield_reason_previously_verified
            is MessageShield.MismatchedSender -> CommonStrings.event_shield_mismatched_sender
        }
    )
}

@Composable
internal fun MessageShieldData.toIcon(): ImageVector {
    return when (shield) {
        is MessageShield.AuthenticityNotGuaranteed -> CompoundIcons.Info()
        is MessageShield.UnknownDevice,
        is MessageShield.UnsignedDevice,
        is MessageShield.UnverifiedIdentity,
        is MessageShield.VerificationViolation,
        is MessageShield.MismatchedSender -> CompoundIcons.HelpSolid()
        is MessageShield.SentInClear -> CompoundIcons.LockOff()
    }
}

@PreviewsDayNight
@Composable
internal fun MessageShieldViewPreview() {
    ElementPreview {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MessageShieldView(
                shield = MessageShieldData(MessageShield.UnknownDevice(true))
            )
            MessageShieldView(
                shield = MessageShieldData(MessageShield.UnverifiedIdentity(true))
            )
            MessageShieldView(
                shield = MessageShieldData(MessageShield.AuthenticityNotGuaranteed(false))
            )
            MessageShieldView(
                shield = MessageShieldData(
                    MessageShield.AuthenticityNotGuaranteed(false),
                    forwarder = UserId("@alice:example.com"),
                )
            )
            MessageShieldView(
                shield = MessageShieldData(
                    MessageShield.AuthenticityNotGuaranteed(false),
                    forwarder = UserId("@alice:example.com"),
                    forwarderProfile = ProfileDetails.Ready(
                        displayName = "Alice",
                        displayNameAmbiguous = false,
                        avatarUrl = null,
                    ),
                )
            )
            MessageShieldView(
                shield = MessageShieldData(MessageShield.UnsignedDevice(false))
            )
            MessageShieldView(
                shield = MessageShieldData(MessageShield.SentInClear(false))
            )
            MessageShieldView(
                shield = MessageShieldData(MessageShield.VerificationViolation(false))
            )
            MessageShieldView(
                shield = MessageShieldData(MessageShield.MismatchedSender(false))
            )
        }
    }
}
