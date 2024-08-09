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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.messageFromMeBackground
import io.element.android.libraries.designsystem.theme.messageFromOtherBackground
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.api.timeline.item.event.ShieldColor

@Composable
internal fun MessageShieldView(
    isMine: Boolean = false,
    shield: MessageShield,
    modifier: Modifier = Modifier
) {
    val borderColor = if (shield.color == ShieldColor.RED) ElementTheme.colors.borderCriticalPrimary else ElementTheme.colors.bgSubtlePrimary
    val iconColor = if (shield.color == ShieldColor.RED) ElementTheme.colors.iconCriticalPrimary else ElementTheme.colors.iconSecondary

    val backgroundBubbleColor = when {
        isMine -> ElementTheme.colors.messageFromMeBackground
        else -> ElementTheme.colors.messageFromOtherBackground
    }
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .background(backgroundBubbleColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Icon(
            imageVector = shield.toIcon(),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = iconColor,
        )
        Spacer(modifier = Modifier.size(4.dp))
        val textColor = if (shield.color == ShieldColor.RED) ElementTheme.colors.textCriticalPrimary else ElementTheme.colors.textSecondary
        Text(
            text = shield.message,
            style = ElementTheme.typography.fontBodyXsRegular,
            color = textColor
        )
    }
}

@Composable
private fun MessageShield.toIcon(): ImageVector {
    return when (this.color) {
        ShieldColor.RED -> CompoundIcons.Error()
        ShieldColor.GREY -> CompoundIcons.InfoSolid()
    }
}

@PreviewsDayNight
@Composable
internal fun MessageShieldViewPreview() {
    ElementPreview {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MessageShieldView(
                shield = MessageShield(
                    message = "The authenticity of this encrypted message can't be guaranteed on this device.",
                    color = ShieldColor.GREY
                )
            )
            MessageShieldView(
                isMine = true,
                shield = MessageShield(
                    message = "The authenticity of this encrypted message can't be guaranteed on this device.",
                    color = ShieldColor.GREY
                )
            )
            MessageShieldView(
                shield = MessageShield(
                    message = "Encrypted by a device not verified by its owner.",
                    color = ShieldColor.RED
                )
            )
            MessageShieldView(
                shield = MessageShield(
                    message = "Encrypted by an unknown or deleted device.",
                    color = ShieldColor.RED
                )
            )
        }
    }
}
