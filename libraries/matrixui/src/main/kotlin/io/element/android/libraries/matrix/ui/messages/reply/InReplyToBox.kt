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

package io.element.android.libraries.matrix.ui.messages.reply

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.atoms.PlaceholderAtom
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnail
import io.element.android.libraries.matrix.ui.messages.sender.SenderName
import io.element.android.libraries.matrix.ui.messages.sender.SenderNameMode
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun InReplyToBox(
    inReplyTo: InReplyToDetails,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (inReplyTo) {
            is InReplyToDetails.Ready -> {
                ReplyToReadyContent(
                    senderId = inReplyTo.senderId,
                    senderProfile = inReplyTo.senderProfile,
                    metadata = inReplyTo.metadata(),
                )
            }
            is InReplyToDetails.Error ->
                ReplyToErrorContent(data = inReplyTo)
            is InReplyToDetails.Loading ->
                ReplyToLoadingContent()
        }
    }
}

@Composable
private fun ReplyToReadyContent(
    senderId: UserId,
    senderProfile: ProfileTimelineDetails,
    metadata: InReplyToMetadata?,
    modifier: Modifier = Modifier,
) {
    val paddings = if (metadata is InReplyToMetadata.Thumbnail) {
        PaddingValues(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
    } else {
        PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    }
    Row(
            modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(paddings)
    ) {
        if (metadata is InReplyToMetadata.Thumbnail) {
            AttachmentThumbnail(
                info = metadata.attachmentThumbnailInfo,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        val a11InReplyToText = stringResource(CommonStrings.common_in_reply_to, senderProfile.getDisambiguatedDisplayName(senderId))
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            SenderName(
                senderId = senderId,
                senderProfile = senderProfile,
                senderNameMode = SenderNameMode.Reply,
                modifier = Modifier.semantics {
                    contentDescription = a11InReplyToText
                },
            )
            ReplyToContentText(metadata)
        }
    }
}

@Composable
private fun ReplyToLoadingContent(
    modifier: Modifier = Modifier,
) {
    val paddings = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    Row(
            modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(paddings)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PlaceholderAtom(width = 80.dp, height = 12.dp)
            PlaceholderAtom(width = 140.dp, height = 14.dp)
        }
    }
}

@Composable
private fun ReplyToErrorContent(
    data: InReplyToDetails.Error,
    modifier: Modifier = Modifier,
) {
    val paddings = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    Row(
            modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(paddings)
    ) {
        Text(
            text = data.message,
            style = ElementTheme.typography.fontBodyMdRegular,
            color = MaterialTheme.colorScheme.error,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ReplyToContentText(metadata: InReplyToMetadata?) {
    val text = when (metadata) {
        InReplyToMetadata.Redacted -> stringResource(id = CommonStrings.common_message_removed)
        InReplyToMetadata.UnableToDecrypt -> stringResource(id = CommonStrings.common_waiting_for_decryption_key)
        is InReplyToMetadata.Text -> metadata.text
        is InReplyToMetadata.Thumbnail -> metadata.text
        null -> ""
    }
    val iconResourceId = when (metadata) {
        InReplyToMetadata.Redacted -> CompoundDrawables.ic_compound_delete
        InReplyToMetadata.UnableToDecrypt -> CompoundDrawables.ic_compound_time
        else -> null
    }
    val fontStyle = when (metadata) {
        is InReplyToMetadata.Informative -> FontStyle.Italic
        else -> FontStyle.Normal
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconResourceId != null) {
            Icon(
                resourceId = iconResourceId,
                tint = MaterialTheme.colorScheme.secondary,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = ElementTheme.typography.fontBodyMdRegular,
            fontStyle = fontStyle,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
