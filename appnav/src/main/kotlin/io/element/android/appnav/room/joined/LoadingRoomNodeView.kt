/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.room.joined

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.networkmonitor.api.ui.ConnectivityIndicatorView
import io.element.android.libraries.designsystem.atomic.molecules.IconTitlePlaceholdersRowMolecule
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.ui.room.LoadingRoomState
import io.element.android.libraries.matrix.ui.room.LoadingRoomStateProvider
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LoadingRoomNodeView(
    state: LoadingRoomState,
    hasNetworkConnection: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                ConnectivityIndicatorView(isOnline = hasNetworkConnection)
                LoadingRoomTopBar(onBackClick)
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state is LoadingRoomState.Error) {
                    Text(
                        text = stringResource(id = CommonStrings.error_unknown),
                        color = ElementTheme.colors.textSecondary,
                        style = ElementTheme.typography.fontBodyMdRegular,
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingRoomTopBar(
    onBackClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
            IconTitlePlaceholdersRowMolecule(iconSize = AvatarSize.TimelineRoom.dp)
        },
        windowInsets = WindowInsets(0.dp),
    )
}

@PreviewsDayNight
@Composable
internal fun LoadingRoomNodeViewPreview(@PreviewParameter(LoadingRoomStateProvider::class) state: LoadingRoomState) = ElementPreview {
    LoadingRoomNodeView(
        state = state,
        onBackClick = {},
        hasNetworkConnection = false
    )
}
