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

package io.element.android.features.roomaliasresolver.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.atoms.PlaceholderAtom
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomAliasResolverView(
    state: RoomAliasResolverState,
    onBackPressed: () -> Unit,
    onAliasResolved: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnAliasResolved by rememberUpdatedState(onAliasResolved)
    LaunchedEffect(state.resolveState) {
        if (state.resolveState is AsyncData.Success) {
            latestOnAliasResolved(state.resolveState.data)
        }
    }

    HeaderFooterPage(
        modifier = modifier,
        paddingValues = PaddingValues(16.dp),
        topBar = {
            RoomAliasResolverTopBar(onBackClicked = onBackPressed)
        },
        content = {
            RoomAliasResolverContent(state = state)
        },
        footer = {
            RoomAliasResolverFooter(
                state = state,
            )
        }
    )
}

@Composable
private fun RoomAliasResolverFooter(
    state: RoomAliasResolverState,
    modifier: Modifier = Modifier,
) {
    when (state.resolveState) {
        is AsyncData.Failure -> {
            Button(
                text = stringResource(CommonStrings.action_retry),
                onClick = {
                    state.eventSink(RoomAliasResolverEvents.Retry)
                },
                modifier = modifier.fillMaxWidth(),
                size = ButtonSize.Medium,
            )
        }
        is AsyncData.Loading -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        AsyncData.Uninitialized,
        is AsyncData.Success -> Unit
    }
}

@Composable
private fun RoomAliasResolverContent(
    state: RoomAliasResolverState,
    modifier: Modifier = Modifier,
) {
    ContentScaffold(
        modifier = modifier,
        avatar = {
            PlaceholderAtom(width = AvatarSize.RoomHeader.dp, height = AvatarSize.RoomHeader.dp)
        },
        title = {
        },
        subtitle = {
            Title(state.roomAlias.value)
        },
        description = {
            if (state.resolveState.isFailure()) {
                Text(
                    text = "Failed to resolve room alias",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        memberCount = {
        }
    )
}

@Composable
private fun ContentScaffold(
    avatar: @Composable () -> Unit,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: @Composable (() -> Unit)? = null,
    memberCount: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        avatar()
        Spacer(modifier = Modifier.height(16.dp))
        title()
        Spacer(modifier = Modifier.height(8.dp))
        subtitle()
        Spacer(modifier = Modifier.height(8.dp))
        if (memberCount != null) {
            memberCount()
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (description != null) {
            description()
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun Title(title: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = title,
        style = ElementTheme.typography.fontHeadingMdBold,
        textAlign = TextAlign.Center,
        color = ElementTheme.colors.textPrimary,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomAliasResolverTopBar(
    onBackClicked: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackButton(onClick = onBackClicked)
        },
        title = {},
    )
}

@PreviewLightDark
@Composable
internal fun RoomAliasResolverViewPreview(@PreviewParameter(RoomAliasResolverStateProvider::class) state: RoomAliasResolverState) = ElementPreview {
    RoomAliasResolverView(
        state = state,
        onAliasResolved = { },
        onBackPressed = { }
    )
}
