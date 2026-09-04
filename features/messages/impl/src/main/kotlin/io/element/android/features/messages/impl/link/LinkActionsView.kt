/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.link

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.androidutils.system.copyToClipboard
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkActionsView(
    url: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val copiedMessage = stringResource(CommonStrings.common_copied_to_clipboard)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrollable = false,
    ) {
        LinkActionsViewContent(
            onOpenWithClick = {
                onDismiss()
                context.openUrlInExternalApp(url)
            },
            onShareClick = {
                onDismiss()
                context.startSharePlainTextIntent(
                    activityResultLauncher = null,
                    chooserTitle = null,
                    text = url,
                )
            },
            onCopyClick = {
                onDismiss()
                context.copyToClipboard(text = url, toastMessage = copiedMessage)
            },
        )
    }
}

@Composable
internal fun LinkActionsViewContent(
    onOpenWithClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyClick: () -> Unit,
) {
    Column {
        ListItem(
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.PopOut())),
            onClick = onOpenWithClick,
            content = { Text(stringResource(CommonStrings.action_open_with)) },
        )
        ListItem(
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.ShareAndroid())),
            onClick = onShareClick,
            content = { Text(stringResource(CommonStrings.action_share_link)) },
        )
        ListItem(
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Copy())),
            onClick = onCopyClick,
            content = { Text(stringResource(CommonStrings.action_copy_link)) },
        )
    }
}

@PreviewsDayNight
@Composable
internal fun LinkActionsViewContentPreview() = ElementPreview {
    LinkActionsViewContent(
        onOpenWithClick = {},
        onShareClick = {},
        onCopyClick = {},
    )
}
