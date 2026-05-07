/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.designsystem.colors.AvatarColorsProvider
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.impl.R
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.strings.Strings

/**
 * Ref: https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=2229-149220
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailsBottomSheet(
    state: MediaBottomSheetState.Details,
    onViewInTimeline: (EventId) -> Unit,
    onShare: (EventId) -> Unit,
    onForward: (EventId) -> Unit,
    onDownload: (EventId) -> Unit,
    onOpenWith: (EventId) -> Unit,
    onDelete: (EventId) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        scrollable = false,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Title()
            Section(
                title = stringResource(R.string.screen_media_details_uploaded_by),
            ) {
                SenderRow(
                    mediaInfo = state.mediaInfo,
                )
            }
            SectionText(
                title = stringResource(R.string.screen_media_details_uploaded_on),
                text = state.mediaInfo.dateSentFull.orEmpty(),
            )
            SectionText(
                title = stringResource(R.string.screen_media_details_filename),
                text = state.mediaInfo.filename,
            )
            SectionText(
                title = stringResource(R.string.screen_media_details_file_format),
                text = state.mediaInfo.mimeType + Strings.NICE_SEPARATOR + state.mediaInfo.formattedFileSize,
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (state.eventId != null) {
                HorizontalDivider()
                ListItem(
                    leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.VisibilityOn())),
                    headlineContent = { Text(stringResource(CommonStrings.action_view_in_timeline)) },
                    style = ListItemStyle.Primary,
                    onClick = {
                        onViewInTimeline(state.eventId)
                    }
                )
                ListItem(
                    leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.ShareAndroid())),
                    headlineContent = { Text(stringResource(CommonStrings.action_share)) },
                    style = ListItemStyle.Primary,
                    onClick = {
                        onShare(state.eventId)
                    }
                )
                ListItem(
                    leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Forward())),
                    headlineContent = { Text(stringResource(CommonStrings.action_forward)) },
                    style = ListItemStyle.Primary,
                    onClick = {
                        onForward(state.eventId)
                    }
                )
                ListItem(
                    leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Download())),
                    headlineContent = { Text(stringResource(CommonStrings.action_download)) },
                    style = ListItemStyle.Primary,
                    onClick = {
                        onDownload(state.eventId)
                    }
                )
                val mimeType = state.mediaInfo.mimeType
                val icon = when (mimeType) {
                    MimeTypes.Apk ->
                        ListItemContent.Icon(IconSource.Resource(R.drawable.ic_apk_install))
                    else ->
                        ListItemContent.Icon(IconSource.Vector(CompoundIcons.PopOut()))
                }
                val wording = when (mimeType) {
                    MimeTypes.Apk -> stringResource(id = CommonStrings.common_install_apk_android)
                    else -> stringResource(id = CommonStrings.action_open_with)
                }
                ListItem(
                    leadingContent = icon,
                    headlineContent = { Text(wording) },
                    style = ListItemStyle.Primary,
                    onClick = {
                        onOpenWith(state.eventId)
                    }
                )
                if (state.canDelete) {
                    HorizontalDivider()
                    ListItem(
                        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Delete())),
                        headlineContent = { Text(stringResource(CommonStrings.action_delete)) },
                        style = ListItemStyle.Destructive,
                        onClick = {
                            onDelete(state.eventId)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SenderRow(
    mediaInfo: MediaInfo,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val id = mediaInfo.senderId?.value ?: "@Alice:domain"
        Avatar(
            avatarData = AvatarData(
                id = id,
                name = mediaInfo.senderName,
                url = mediaInfo.senderAvatar,
                size = AvatarSize.MediaSender,
            ),
            avatarType = AvatarType.User,
        )
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
        ) {
            // Name
            val bestName = mediaInfo.senderName ?: mediaInfo.senderId?.value.orEmpty()
            val avatarColors = AvatarColorsProvider.provide(id)
            Text(
                modifier = Modifier.clipToBounds(),
                text = bestName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = avatarColors.foreground,
                style = ElementTheme.typography.fontBodyMdMedium,
            )
            // Id
            if (!mediaInfo.senderName.isNullOrEmpty()) {
                Text(
                    text = mediaInfo.senderId?.value.orEmpty(),
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ElementTheme.typography.fontBodyMdRegular,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.Title() {
    Text(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            .semantics {
                heading()
            },
        text = stringResource(R.string.screen_media_details_title),
        textAlign = TextAlign.Center,
        style = ElementTheme.typography.fontBodyLgMedium,
        color = ElementTheme.colors.textPrimary,
    )
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = ElementTheme.typography.fontBodyMdMedium,
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
internal fun MediaDetailsBottomSheetPreview(
    @PreviewParameter(MediaBottomSheetStateDetailsProvider::class) state: MediaBottomSheetState.Details,
) = ElementPreview {
    MediaDetailsBottomSheet(
        state = state,
        onViewInTimeline = {},
        onShare = {},
        onForward = {},
        onDownload = {},
        onOpenWith = {},
        onDelete = {},
        onDismiss = {},
    )
}
