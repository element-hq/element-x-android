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

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.icons.CompoundIcons
import io.element.android.compound.icons.compoundicons.Check
import io.element.android.compound.icons.compoundicons.Send
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun SendButton(
    canSendMessage: Boolean,
    onClick: () -> Unit,
    composerMode: MessageComposerMode,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier
            .size(48.dp),
        onClick = onClick,
        enabled = canSendMessage,
    ) {
        val iconVector = when (composerMode) {
            is MessageComposerMode.Edit -> CompoundIcons.Check
            else -> CompoundIcons.Send
        }
        val iconStartPadding = when (composerMode) {
            is MessageComposerMode.Edit -> 0.dp
            else -> 2.dp
        }
        val contentDescription = when (composerMode) {
            is MessageComposerMode.Edit -> stringResource(CommonStrings.action_edit)
            else -> stringResource(CommonStrings.action_send)
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(36.dp)
                .background(if (canSendMessage) ElementTheme.colors.iconAccentTertiary else Color.Transparent)
        ) {
            Icon(
                modifier = Modifier
                    .padding(start = iconStartPadding)
                    .align(Alignment.Center),
                imageVector = iconVector,
                contentDescription = contentDescription,
                // Exception here, we use Color.White instead of ElementTheme.colors.iconOnSolidPrimary
                tint = if (canSendMessage) Color.White else ElementTheme.colors.iconDisabled
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SendButtonPreview() = ElementPreview {
    val normalMode = MessageComposerMode.Normal
    val editMode = MessageComposerMode.Edit(null, "", null)
    Row {
        SendButton(canSendMessage = true, onClick = {}, composerMode = normalMode)
        SendButton(canSendMessage = false, onClick = {}, composerMode = normalMode)
        SendButton(canSendMessage = true, onClick = {}, composerMode = editMode)
        SendButton(canSendMessage = false, onClick = {}, composerMode = editMode)
    }
}
