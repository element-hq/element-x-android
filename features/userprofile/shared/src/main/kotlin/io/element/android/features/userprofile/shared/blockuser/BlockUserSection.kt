/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.userprofile.shared.blockuser

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.userprofile.api.UserProfileEvents
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.shared.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun BlockUserSection(
    state: UserProfileState,
    modifier: Modifier = Modifier,
) {
    val isBlocked = state.isBlocked
    PreferenceCategory(
        modifier = modifier,
        showTopDivider = false,
    ) {
        when (isBlocked) {
            is AsyncData.Failure -> PreferenceBlockUser(isBlocked = isBlocked.prevData, isLoading = false, eventSink = state.eventSink)
            is AsyncData.Loading -> PreferenceBlockUser(isBlocked = isBlocked.prevData, isLoading = true, eventSink = state.eventSink)
            is AsyncData.Success -> PreferenceBlockUser(isBlocked = isBlocked.data, isLoading = false, eventSink = state.eventSink)
            AsyncData.Uninitialized -> PreferenceBlockUser(isBlocked = null, isLoading = true, eventSink = state.eventSink)
        }
    }
    if (isBlocked is AsyncData.Failure) {
        RetryDialog(
            content = stringResource(CommonStrings.error_unknown),
            onDismiss = { state.eventSink(UserProfileEvents.ClearBlockUserError) },
            onRetry = {
                val event = when (isBlocked.prevData) {
                    true -> UserProfileEvents.UnblockUser(needsConfirmation = false)
                    false -> UserProfileEvents.BlockUser(needsConfirmation = false)
                    // null case Should not happen
                    null -> UserProfileEvents.ClearBlockUserError
                }
                state.eventSink(event)
            },
        )
    }
}

@Composable
private fun PreferenceBlockUser(
    isBlocked: Boolean?,
    isLoading: Boolean,
    eventSink: (UserProfileEvents) -> Unit,
) {
    val loadingCurrentValue = @Composable {
        CircularProgressIndicator(
            modifier = Modifier
                .progressSemantics()
                .size(20.dp),
            strokeWidth = 2.dp
        )
    }
    if (isBlocked.orFalse()) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_dm_details_unblock_user)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Block())),
            onClick = { if (!isLoading) eventSink(UserProfileEvents.UnblockUser(needsConfirmation = true)) },
            trailingContent = if (isLoading) ListItemContent.Custom(loadingCurrentValue) else null,
            style = ListItemStyle.Primary,
        )
    } else {
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_dm_details_block_user)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Block())),
            style = ListItemStyle.Destructive,
            onClick = { if (!isLoading) eventSink(UserProfileEvents.BlockUser(needsConfirmation = true)) },
            trailingContent = if (isLoading) ListItemContent.Custom(loadingCurrentValue) else null,
        )
    }
}
