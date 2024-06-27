/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.textcomposer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToView
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun ComposerModeView(
    composerMode: MessageComposerMode,
    onResetComposerMode: () -> Unit,
) {
    when (composerMode) {
        is MessageComposerMode.Edit -> {
            EditingModeView(onResetComposerMode = onResetComposerMode)
        }
        is MessageComposerMode.Reply -> {
            ReplyToModeView(
                modifier = Modifier.padding(8.dp),
                replyToDetails = composerMode.replyToDetails,
                onResetComposerMode = onResetComposerMode,
            )
        }
        else -> Unit
    }
}

@Composable
private fun EditingModeView(
    onResetComposerMode: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
    ) {
        Icon(
            imageVector = CompoundIcons.Edit(),
            contentDescription = stringResource(CommonStrings.common_editing),
            tint = ElementTheme.materialColors.secondary,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(16.dp),
        )
        Text(
            stringResource(CommonStrings.common_editing),
            style = ElementTheme.typography.fontBodySmRegular,
            textAlign = TextAlign.Start,
            color = ElementTheme.materialColors.secondary,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .weight(1f)
        )
        Icon(
            imageVector = CompoundIcons.Close(),
            contentDescription = stringResource(CommonStrings.action_close),
            tint = ElementTheme.materialColors.secondary,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 12.dp)
                .size(16.dp)
                .clickable(
                    enabled = true,
                    onClick = onResetComposerMode,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false)
                ),
        )
    }
}

@Composable
private fun ReplyToModeView(
    replyToDetails: InReplyToDetails,
    onResetComposerMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
    ) {
        InReplyToView(inReplyTo = replyToDetails, modifier = Modifier.weight(1f))
        Icon(
            imageVector = CompoundIcons.Close(),
            contentDescription = stringResource(CommonStrings.action_close),
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .padding(end = 4.dp, top = 4.dp, start = 16.dp, bottom = 16.dp)
                .size(16.dp)
                .clickable(
                    enabled = true,
                    onClick = onResetComposerMode,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false)
                ),
        )
    }
}
