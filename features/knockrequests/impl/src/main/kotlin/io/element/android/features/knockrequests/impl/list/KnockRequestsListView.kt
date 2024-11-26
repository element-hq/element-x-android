/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.knockrequests.impl.KnockRequest
import io.element.android.features.knockrequests.impl.getAvatarData
import io.element.android.features.knockrequests.impl.getBestName
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Composable
fun KnockRequestsListView(
    state: KnockRequestsListState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            KnockRequestsListTopBar(onBackClick = onBackClick)
        },
        content = { padding ->
            KnockRequestsListContent(
                state = state,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
            )
        }
    )
}

@Composable
private fun KnockRequestsListContent(state: KnockRequestsListState, modifier: Modifier) {

    fun onAcceptClick(knockRequest: KnockRequest) {
        state.eventSink(KnockRequestsListEvents.Accept(knockRequest))
    }

    fun onDeclineClick(knockRequest: KnockRequest) {
        state.eventSink(KnockRequestsListEvents.Decline(knockRequest))
    }

    Box(modifier, contentAlignment = Alignment.Center) {
        when (state.knockRequests) {
            is AsyncData.Success -> {
                val knockRequests = state.knockRequests.data
                if (knockRequests.isEmpty()) {
                    KnockRequestsListEmpty()
                } else {
                    KnockRequestsList(
                        knockRequests = knockRequests,
                        onAcceptClick = ::onAcceptClick,
                        onDeclineClick = ::onDeclineClick,
                    )
                }
            }
            else -> Unit
        }
        KnockRequestsActionsView(
            actions = state.currentAction,
            onDismiss = {
                state.eventSink(KnockRequestsListEvents.AcceptAll)
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun KnockRequestsActionsView(
    actions: KnockRequestsCurrentAction,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        when (actions) {
            is KnockRequestsCurrentAction.AcceptAll -> {
                AsyncActionView(
                    async = actions.async,
                    onSuccess = {},
                    onErrorDismiss = onDismiss,
                )
            }
            is KnockRequestsCurrentAction.Accept -> {
                AsyncActionView(
                    async = actions.async,
                    onSuccess = {},
                    onErrorDismiss = onDismiss,
                )
            }
            is KnockRequestsCurrentAction.Decline -> {
                AsyncActionView(
                    async = actions.async,
                    onSuccess = {},
                    onErrorDismiss = onDismiss,
                )
            }
            KnockRequestsCurrentAction.None -> Unit
        }
    }
}

@Composable
private fun KnockRequestsList(
    knockRequests: ImmutableList<KnockRequest>,
    onAcceptClick: (KnockRequest) -> Unit,
    onDeclineClick: (KnockRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(knockRequests) { index, knockRequest ->
            KnockRequestItem(
                knockRequest = knockRequest,
                onAcceptClick = onAcceptClick,
                onDeclineClick = onDeclineClick,
            )
            if (index != knockRequests.size - 1) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun KnockRequestItem(
    knockRequest: KnockRequest,
    onAcceptClick: (KnockRequest) -> Unit,
    onDeclineClick: (KnockRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Avatar(knockRequest.getAvatarData(AvatarSize.KnockRequestItem))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier) {
            // Name
            Text(
                modifier = Modifier.clipToBounds(),
                text = knockRequest.getBestName(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
                style = ElementTheme.typography.fontBodyLgMedium,
            )
            // UserId
            if (!knockRequest.displayName.isNullOrEmpty()) {
                Text(
                    text = knockRequest.userId.value,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ElementTheme.typography.fontBodyMdRegular,
                )
            }
            // Reason
            if (!knockRequest.reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = knockRequest.reason,
                    style = ElementTheme.typography.fontBodyMdRegular,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    text = stringResource(CommonStrings.action_decline),
                    onClick = {
                        onDeclineClick(knockRequest)
                    },
                    size = ButtonSize.MediumLowPadding,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    text = stringResource(CommonStrings.action_accept),
                    onClick = {
                        onAcceptClick(knockRequest)
                    },
                    size = ButtonSize.MediumLowPadding,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                text = stringResource(CommonStrings.screen_knock_requests_list_decline_and_ban_action_title),
                onClick = {
                    onAcceptClick(knockRequest)
                },
                destructive = true,
                size = ButtonSize.Small,
                modifier = Modifier.fillMaxWidth(),
            )

        }
    }
}

@Composable
private fun KnockRequestsListEmpty(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(
            horizontal = 32.dp,
            vertical = 48.dp,
        ),
        contentAlignment = Alignment.Center,
    ) {
        IconTitleSubtitleMolecule(
            title = "No pending request to join",
            subTitle = "When somebody will ask to join the room, you’ll be able to see their request here.",
            iconStyle = BigIcon.Style.Default(CompoundIcons.Pin()),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KnockRequestsListTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Requests to join",
                style = ElementTheme.typography.aliasScreenTitle,
            )
        },
        navigationIcon = { BackButton(onClick = onBackClick) },
    )
}

@PreviewsDayNight
@Composable
internal fun KnockRequestsListViewPreview(
    @PreviewParameter(KnockRequestsListStateProvider::class) state: KnockRequestsListState
) = ElementPreview {
    KnockRequestsListView(
        state = state,
        onBackClick = {},
    )
}
