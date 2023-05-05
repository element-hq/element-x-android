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

package io.element.android.features.roomdetails.impl.members.search.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.roomdetails.impl.members.search.MemberListEvents
import io.element.android.features.roomdetails.impl.members.search.MemberListState
import io.element.android.features.roomdetails.impl.members.search.MemberListStateProvider
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.matrix.api.user.MatrixUser

@Composable
fun MemberListView(
    state: MemberListState,
    modifier: Modifier = Modifier,
    onUserSelected: (MatrixUser) -> Unit = {},
    onUserDeselected: (MatrixUser) -> Unit = {},
) {
    Column(
        modifier = modifier,
    ) {
        SearchMemberBar(
            modifier = Modifier.fillMaxWidth(),
            query = state.searchQuery,
            state = state.searchResults,
            selectedUsers = state.selectedUsers,
            active = state.isSearchActive,
            isMultiSelectionEnabled = state.isMultiSelectionEnabled,
            onActiveChanged = { state.eventSink(MemberListEvents.OnSearchActiveChanged(it)) },
            onTextChanged = { state.eventSink(MemberListEvents.UpdateSearchQuery(it)) },
            onUserSelected = {
                state.eventSink(MemberListEvents.AddToSelection(it))
                onUserSelected(it)
            },
            onUserDeselected = {
                state.eventSink(MemberListEvents.RemoveFromSelection(it))
                onUserDeselected(it)
            },
        )

        if (state.isMultiSelectionEnabled && !state.isSearchActive && state.selectedUsers.isNotEmpty()) {
            SelectedMembersList(
                contentPadding = PaddingValues(16.dp),
                selectedUsers = state.selectedUsers,
                autoScroll = true,
                onUserRemoved = {
                    state.eventSink(MemberListEvents.RemoveFromSelection(it))
                    onUserDeselected(it)
                },
            )
        }
    }
}

@Preview
@Composable
internal fun UserListViewLightPreview(@PreviewParameter(MemberListStateProvider::class) state: MemberListState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun UserListViewDarkPreview(@PreviewParameter(MemberListStateProvider::class) state: MemberListState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: MemberListState) {
    MemberListView(state = state)
}
