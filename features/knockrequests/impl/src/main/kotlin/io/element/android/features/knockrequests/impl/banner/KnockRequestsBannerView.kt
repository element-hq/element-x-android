/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.knockrequests.impl.KnockRequest
import io.element.android.features.knockrequests.impl.getAvatarData
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
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

private const val MAX_AVATAR_COUNT = 3

@Composable
fun KnockRequestsBannerView(
    state: KnockRequestsBannerState,
    onViewRequestsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier,
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

    Column(
        modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
    ) {
        Row {
            KnockRequestAvatarView(state.knockRequests)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.formattedTitle(),
                    style = ElementTheme.typography.fontBodyMdMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                )
                if (state.subtitle != null) {
                    Text(
                        text = state.subtitle,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Start,
                    )
                }
            }
            Icon(
                modifier = Modifier.clickable(onClick = ::onDismissClick),
                imageVector = CompoundIcons.Close(),
                contentDescription = stringResource(CommonStrings.action_close)
            )
        }
        if (state.reason != null) {
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
                    text = "View all",
                    onClick = onViewRequestsClick,
                    size = ButtonSize.MediumLowPadding,
                    modifier = Modifier.weight(1f),
                )
            } else {
                OutlinedButton(
                    text = "View",
                    onClick = onViewRequestsClick,
                    size = ButtonSize.MediumLowPadding,
                    modifier = Modifier.weight(1f),
                )
                if (state.canAccept) {
                    Button(
                        text = "Accept",
                        onClick = {},
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
    knockRequests: ImmutableList<KnockRequest>,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        when (knockRequests.size) {
            0 -> Unit
            1 -> Avatar(knockRequests.first().getAvatarData(AvatarSize.KnockRequestBanner))
            else -> KnockRequestAvatarListView(knockRequests)
        }
    }
}

@Composable
private fun KnockRequestAvatarListView(
    knockRequests: ImmutableList<KnockRequest>,
    modifier: Modifier = Modifier,
) {
    val avatarSize = AvatarSize.KnockRequestBanner.dp
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(-avatarSize / 2),
    ) {
        knockRequests
            .take(MAX_AVATAR_COUNT)
            .forEachIndexed { index, knockRequest ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(size = avatarSize)
                        .clip(CircleShape)
                        .background(color = ElementTheme.colors.bgCanvasDefaultLevel1)
                        .zIndex(-index.toFloat()),
                ) {
                    Avatar(
                        modifier = Modifier.padding(2.dp),
                        avatarData = knockRequest.getAvatarData(AvatarSize.KnockRequestBanner),
                    )
                }
            }
    }
}

@Composable
@PreviewsDayNight
internal fun KnockRequestsBannerViewPreview(@PreviewParameter(KnockRequestsBannerStateProvider::class) state: KnockRequestsBannerState) = ElementPreview {
    KnockRequestsBannerView(
        state = state,
        onViewRequestsClick = {},
    )
}
