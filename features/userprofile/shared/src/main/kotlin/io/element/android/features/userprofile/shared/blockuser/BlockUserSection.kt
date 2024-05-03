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

package io.element.android.features.userprofile.shared.blockuser

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.userprofile.shared.R
import io.element.android.features.userprofile.shared.UserProfileEvents
import io.element.android.features.userprofile.shared.UserProfileState
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
    PreferenceCategory(
        modifier = modifier,
        showDivider = false,
    ) {
        when (state.isBlocked) {
            is AsyncData.Failure -> PreferenceBlockUser(isBlocked = state.isBlocked.prevData, isLoading = false, eventSink = state.eventSink)
            is AsyncData.Loading -> PreferenceBlockUser(isBlocked = state.isBlocked.prevData, isLoading = true, eventSink = state.eventSink)
            is AsyncData.Success -> PreferenceBlockUser(isBlocked = state.isBlocked.data, isLoading = false, eventSink = state.eventSink)
            AsyncData.Uninitialized -> PreferenceBlockUser(isBlocked = null, isLoading = true, eventSink = state.eventSink)
        }
    }
    if (state.isBlocked is AsyncData.Failure) {
        RetryDialog(
            content = stringResource(CommonStrings.error_unknown),
            onDismiss = { state.eventSink(UserProfileEvents.ClearBlockUserError) },
            onRetry = {
                val event = when (state.isBlocked.prevData) {
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
