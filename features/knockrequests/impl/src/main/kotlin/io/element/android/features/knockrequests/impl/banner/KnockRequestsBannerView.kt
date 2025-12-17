/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.knockrequests.impl.R
import io.element.android.features.knockrequests.impl.data.KnockRequestPresentable
import io.element.android.libraries.designsystem.components.async.AsyncIndicator
import io.element.android.libraries.designsystem.components.async.AsyncIndicatorHost
import io.element.android.libraries.designsystem.components.async.rememberAsyncIndicatorState
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarRow
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private const val MAX_AVATAR_COUNT = 3

@Composable
fun KnockRequestsBannerView(
    state: KnockRequestsBannerState,
    onViewRequestsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = state.isVisible,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = ElementTheme.colors.bgCanvasDefaultLevel1,
                shadowElevation = 24.dp,
                modifier = Modifier.padding(16.dp),
            ) {
                KnockRequestsBannerContent(
                    state = state,
                    onViewRequestsClick = onViewRequestsClick,
                )
            }
        }
        KnockRequestsAcceptErrorView(displayError = state.displayAcceptError)
    }
}

@Composable
private fun KnockRequestsAcceptErrorView(
    displayError: Boolean,
    modifier: Modifier = Modifier,
) {
    val asyncIndicatorState = rememberAsyncIndicatorState()
    AsyncIndicatorHost(modifier = modifier.statusBarsPadding(), state = asyncIndicatorState)
    LaunchedEffect(displayError) {
        if (displayError) {
            asyncIndicatorState.enqueue {
                AsyncIndicator.Custom(text = stringResource(CommonStrings.error_unknown))
            }
        } else {
            asyncIndicatorState.clear()
        }
    }
}

@Composable
private fun KnockRequestsBannerContent(
    state: KnockRequestsBannerState,
    onViewRequestsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onDismissClick() {
        state.eventSink(KnockRequestsBannerEvents.Dismiss)
    }

    fun onAcceptClick() {
        state.eventSink(KnockRequestsBannerEvents.AcceptSingleRequest)
    }

    Column(
        modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
    ) {
        Row {
            KnockRequestAvatarView(
                state.knockRequests,
                modifier = Modifier.padding(top = 2.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.formattedTitle(),
                    style = ElementTheme.typography.fontBodyMdMedium,
                    color = ElementTheme.colors.textPrimary,
                    textAlign = TextAlign.Start,
                )
                if (state.subtitle != null) {
                    Text(
                        text = state.subtitle,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                        textAlign = TextAlign.Start,
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                modifier = Modifier.clickable(onClick = ::onDismissClick),
                imageVector = CompoundIcons.Close(),
                contentDescription = stringResource(CommonStrings.action_close)
            )
        }
        val reason = state.reason
        if (!reason.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.reason,
                color = ElementTheme.colors.textPrimary,
                style = ElementTheme.typography.fontBodyMdRegular,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.knockRequests.size > 1) {
                Button(
                    text = stringResource(R.string.screen_room_multiple_knock_requests_view_all_button_title),
                    onClick = onViewRequestsClick,
                    size = ButtonSize.MediumLowPadding,
                    modifier = Modifier.weight(1f),
                )
            } else {
                OutlinedButton(
                    text = stringResource(R.string.screen_room_single_knock_request_view_button_title),
                    onClick = onViewRequestsClick,
                    size = ButtonSize.MediumLowPadding,
                    modifier = Modifier.weight(1f),
                )
                if (state.canAccept) {
                    Button(
                        text = stringResource(R.string.screen_room_single_knock_request_accept_button_title),
                        onClick = ::onAcceptClick,
                        size = ButtonSize.MediumLowPadding,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun KnockRequestAvatarView(
    knockRequests: ImmutableList<KnockRequestPresentable>,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        when (knockRequests.size) {
            0 -> Unit
            1 -> Avatar(
                avatarData = knockRequests.first().getAvatarData(AvatarSize.KnockRequestBanner),
                avatarType = AvatarType.User,
            )
            else -> KnockRequestAvatarListView(knockRequests)
        }
    }
}

@Composable
private fun KnockRequestAvatarListView(
    knockRequests: ImmutableList<KnockRequestPresentable>,
    modifier: Modifier = Modifier,
) {
    val avatars = knockRequests
        .take(MAX_AVATAR_COUNT)
        .map { knockRequest ->
            knockRequest.getAvatarData(AvatarSize.KnockRequestBanner)
        }
        .toImmutableList()
    AvatarRow(
        avatarDataList = avatars,
        avatarType = AvatarType.User,
        modifier = modifier,
    )
}

@Composable
@PreviewsDayNight
internal fun KnockRequestsBannerViewPreview(@PreviewParameter(KnockRequestsBannerStateProvider::class) state: KnockRequestsBannerState) = ElementPreview {
    KnockRequestsBannerView(
        state = state,
        onViewRequestsClick = {},
    )
}
