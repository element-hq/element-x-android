/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        }
    )
}

@Composable
internal fun MessageShield.toIcon(): ImageVector {
    return when (this) {
        is MessageShield.AuthenticityNotGuaranteed -> CompoundIcons.Info()
        is MessageShield.UnknownDevice,
        is MessageShield.UnsignedDevice,
        is MessageShield.UnverifiedIdentity -> CompoundIcons.HelpSolid()
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
        }
    }
}
