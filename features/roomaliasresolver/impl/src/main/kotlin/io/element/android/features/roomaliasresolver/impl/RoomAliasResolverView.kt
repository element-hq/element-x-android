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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.atoms.PlaceholderAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewTitleAtom
import io.element.android.libraries.designsystem.atomic.organisms.RoomPreviewOrganism
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.background.LightGradientBackground
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
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
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        LightGradientBackground()
        HeaderFooterPage(
            containerColor = Color.Transparent,
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
                size = ButtonSize.Large,
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
    RoomPreviewOrganism(
        modifier = modifier,
        avatar = {
            PlaceholderAtom(width = AvatarSize.RoomHeader.dp, height = AvatarSize.RoomHeader.dp)
        },
        title = {
            RoomPreviewTitleAtom(state.roomAlias.value)
        },
        subtitle = {
        },
        description = {
            if (state.resolveState.isFailure()) {
                Text(
                    text = stringResource(id = R.string.screen_room_alias_resolver_resolve_alias_failure),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        memberCount = {
        }
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

@PreviewsDayNight
@Composable
internal fun RoomAliasResolverViewPreview(@PreviewParameter(RoomAliasResolverStateProvider::class) state: RoomAliasResolverState) = ElementPreview {
    RoomAliasResolverView(
        state = state,
        onAliasResolved = { },
        onBackPressed = { }
    )
}
