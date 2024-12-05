/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.colors.AvatarColorsProvider
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.impl.R
import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerEvents
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailsBottomSheet(
    eventId: EventId?,
    canDelete: Boolean,
    mediaInfo: MediaInfo,
    onDismiss: () -> Unit,
    eventSink: (MediaViewerEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Section(
                title = stringResource(R.string.screen_media_details_uploaded_by),
            ) {
                // TODO Color
                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val id = mediaInfo.senderId?.value ?: "@Alice:domain"
                    Avatar(
                        AvatarData(
                            id = id,
                            name = mediaInfo.senderName,
                            url = mediaInfo.senderAvatar,
                            size = AvatarSize.MediaSender,
                        )
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f),
                    ) {
                        // Name
                        val avatarColors = AvatarColorsProvider.provide(id)
                        Text(
                            modifier = Modifier.clipToBounds(),
                            text = mediaInfo.senderName.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = avatarColors.foreground,
                            style = ElementTheme.typography.fontBodyMdMedium,
                        )
                        // Id
                        Text(
                            text = mediaInfo.senderId?.value.orEmpty(),
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = ElementTheme.typography.fontBodyMdRegular,
                        )
                    }
                }

            }
            SectionText(
                title = stringResource(R.string.screen_media_details_uploaded_on),
                text = mediaInfo.dateSent.orEmpty(),
            )
            SectionText(
                title = stringResource(R.string.screen_media_details_filename),
                text = mediaInfo.filename,
            )
            SectionText(
                title = stringResource(R.string.screen_media_details_file_format),
                text = mediaInfo.mimeType + " - " + mediaInfo.formattedFileSize,
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (eventId != null) {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        onDismiss()
                        eventSink(MediaViewerEvents.ViewInTimeline(eventId))
                    },
                    text = stringResource(CommonStrings.action_view_in_timeline),
                    leadingIcon = IconSource.Vector(CompoundIcons.VisibilityOn())
                )
                if (canDelete) {
                    TextButton(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = {
                            onDismiss()
                            eventSink(MediaViewerEvents.Delete(eventId))
                        },
                        destructive = true,
                        text = stringResource(CommonStrings.action_remove),
                        leadingIcon = IconSource.Vector(CompoundIcons.Delete())
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = ElementTheme.typography.fontBodySmMedium,
            color = ElementTheme.colors.textSecondary,
        )
        content()
    }
}

@Composable
private fun SectionText(
    title: String,
    text: String,
) {
    Section(title = title) {
        Text(
            text = text,
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textPrimary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun MediaDetailsBottomSheetPreview() = ElementPreview {
    MediaDetailsBottomSheet(
        eventId = EventId("\$eventId"),
        canDelete = true,
        mediaInfo = anImageMediaInfo(
            senderName = "Alice",
        ),
        onDismiss = {},
        eventSink = {},
    )
}
