/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomaliasresolver.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.atoms.PlaceholderAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewSubtitleAtom
import io.element.android.libraries.designsystem.atomic.organisms.RoomPreviewOrganism
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomAliasResolverView(
    state: RoomAliasResolverState,
    onBackClick: () -> Unit,
    onSuccess: (ResolvedRoomAlias) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        HeaderFooterPage(
            containerColor = Color.Transparent,
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 32.dp
            ),
            topBar = {
                RoomAliasResolverTopBar(onBackClick = onBackClick)
            },
            content = {
                RoomAliasResolverContent(roomAlias = state.roomAlias, isLoading = state.resolveState.isLoading())
            },
        )
        ResolvedRoomAliasView(
            resolvedRoomAlias = state.resolveState,
            onSuccess = onSuccess,
            onRetry = { state.eventSink(RoomAliasResolverEvents.Retry) },
            onDismissError = {
                state.eventSink(RoomAliasResolverEvents.DismissError)
                onBackClick()
            }
        )
    }
}

@Composable
private fun ResolvedRoomAliasView(
    resolvedRoomAlias: AsyncData<ResolvedRoomAlias>,
    onSuccess: (ResolvedRoomAlias) -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
) {
    when (resolvedRoomAlias) {
        is AsyncData.Success -> {
            val latestOnSuccess by rememberUpdatedState(onSuccess)
            LaunchedEffect(Unit) {
                latestOnSuccess(resolvedRoomAlias.data)
            }
        }
        is AsyncData.Failure -> {
            if (resolvedRoomAlias.error is RoomAliasResolverFailures.UnknownAlias) {
                ErrorDialog(
                    title = stringResource(id = R.string.screen_join_room_loading_alert_title),
                    content = stringResource(id = R.string.screen_room_alias_resolver_resolve_alias_failure),
                    onSubmit = onDismissError
                )
            } else {
                RetryDialog(
                    title = stringResource(id = R.string.screen_join_room_loading_alert_title),
                    content = stringResource(id = CommonStrings.error_network_or_server_issue),
                    onRetry = onRetry,
                    onDismiss = onDismissError
                )
            }
        }
        else -> Unit
    }
}

@Composable
private fun RoomAliasResolverContent(
    roomAlias: RoomAlias,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    RoomPreviewOrganism(
        modifier = modifier,
        avatar = {
            PlaceholderAtom(width = AvatarSize.RoomPreviewHeader.dp, height = AvatarSize.RoomPreviewHeader.dp)
        },
        title = {
            RoomPreviewSubtitleAtom(roomAlias.value)
        },
        subtitle = {
            if (isLoading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator()
            }
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
