/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomaliasresolver.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
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
import io.element.android.compound.theme.ElementTheme
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
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomAliasResolverView(
    state: RoomAliasResolverState,
    onBackClick: () -> Unit,
    onSuccess: (ResolvedRoomAlias) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnSuccess by rememberUpdatedState(onSuccess)
    LaunchedEffect(state.resolveState) {
        if (state.resolveState is AsyncData.Success) {
            latestOnSuccess(state.resolveState.data)
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
                RoomAliasResolverTopBar(onBackClick = onBackClick)
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
                    color = ElementTheme.colors.textCriticalPrimary,
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
    onBackClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {},
    )
}

@PreviewsDayNight
@Composable
internal fun RoomAliasResolverViewPreview(@PreviewParameter(RoomAliasResolverStateProvider::class) state: RoomAliasResolverState) = ElementPreview {
    RoomAliasResolverView(
        state = state,
        onSuccess = { },
        onBackClick = { }
    )
}
