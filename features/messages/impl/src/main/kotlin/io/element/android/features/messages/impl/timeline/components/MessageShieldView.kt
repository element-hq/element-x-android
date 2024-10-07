/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.api.timeline.item.event.isCritical
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun MessageShieldView(
    shield: MessageShield,
    modifier: Modifier = Modifier
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

@Composable
internal fun MessageShield.toIconColor(): Color {
    return when (isCritical) {
        true -> ElementTheme.colors.iconCriticalPrimary
        false -> ElementTheme.colors.iconSecondary
    }
}

@Composable
private fun MessageShield.toTextColor(): Color {
    return when (isCritical) {
        true -> ElementTheme.colors.textCriticalPrimary
        false -> ElementTheme.colors.textSecondary
    }
}

@Composable
internal fun MessageShield.toText(): String {
    return stringResource(
        id = when (this) {
            is MessageShield.AuthenticityNotGuaranteed -> CommonStrings.event_shield_reason_authenticity_not_guaranteed
            is MessageShield.UnknownDevice -> CommonStrings.event_shield_reason_unknown_device
            is MessageShield.UnsignedDevice -> CommonStrings.event_shield_reason_unsigned_device
            is MessageShield.UnverifiedIdentity -> CommonStrings.event_shield_reason_unverified_identity
            is MessageShield.SentInClear -> CommonStrings.event_shield_reason_sent_in_clear
            is MessageShield.VerificationViolation -> CommonStrings.event_shield_reason_previously_verified
        }
    )
}

@Composable
internal fun MessageShield.toIcon(): ImageVector {
    return when (this) {
        is MessageShield.AuthenticityNotGuaranteed -> CompoundIcons.Info()
        is MessageShield.UnknownDevice,
        is MessageShield.UnsignedDevice,
        is MessageShield.UnverifiedIdentity,
        is MessageShield.VerificationViolation -> CompoundIcons.HelpSolid()
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
                shield = MessageShield.UnknownDevice(true)
            )
            MessageShieldView(
                shield = MessageShield.UnverifiedIdentity(true)
            )
            MessageShieldView(
                shield = MessageShield.AuthenticityNotGuaranteed(false)
            )
            MessageShieldView(
                shield = MessageShield.UnsignedDevice(false)
            )
            MessageShieldView(
                shield = MessageShield.SentInClear(false)
            )
            MessageShieldView(
                shield = MessageShield.VerificationViolation(false)
            )
        }
    }
}
