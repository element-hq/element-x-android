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

package io.element.android.appnav.room

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoadingRoomNodeView(
    state: LoadingRoomState,
    hasNetworkConnection: Boolean,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                ConnectivityIndicatorView(isOnline = hasNetworkConnection)
                LoadingRoomTopBar(onBackClicked)
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
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            BackButton(onClick = onBackClicked)
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
        onBackClicked = {},
        hasNetworkConnection = false
    )
}
