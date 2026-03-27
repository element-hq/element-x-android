/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.extensions.impl

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionsView(
    state: ExtensionsState,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = stringResource(R.string.screen_extensions_title),
                navigationIcon = {
                    BackButton(onClick = goBack)
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            items(state.extensions) { extension ->
                ExtensionListItem(
                    extension = extension,
                    onClick = { state.eventSink(ExtensionsEvents.OnExtensionClicked(extension)) },
                )
            }
        }
    }
}

@Composable
fun ExtensionListItem(
    extension: ExtensionItem,
    onClick: () -> Unit,
) {
    val leadingContent = if (extension.avatarUrl != null) {
        // TODO add proper avatar
//        ListItemContent.Avatar(
//            avatarData = AvatarData(
//                id = extension.stateKey,
//                name = extension.name,
//                url = extension.avatarUrl,
//                size = AvatarSize.RoomListItem,
//            )
//        )
        ListItemContent.Icon(IconSource.Vector(CompoundIcons.Extensions()))
    } else {
        ListItemContent.Icon(IconSource.Vector(CompoundIcons.Extensions()))
    }
    ListItem(
        headlineContent = { Text(extension.name) },
        leadingContent = leadingContent,
        onClick = onClick,
    )
}

@PreviewsDayNight
@Composable
internal fun ExtensionsViewPreview(
    @PreviewParameter(ExtensionsStateProvider::class) state: ExtensionsState,
) = ElementPreview {
    ExtensionsView(
        state = state,
        goBack = {},
    )
}

