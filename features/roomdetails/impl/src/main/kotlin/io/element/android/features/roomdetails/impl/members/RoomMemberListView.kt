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

package io.element.android.features.roomdetails.impl.members

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.roomdetails.impl.R
import io.element.android.features.roomdetails.impl.members.search.components.SearchSingleMemberResultItem
import io.element.android.features.roomdetails.impl.members.search.components.MemberListView
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.isLoading
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomMemberListView(
    state: RoomMemberListState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onMemberSelected: (UserId) -> Unit = {},
) {

    fun onUserSelected(user: MatrixUser) {
        onMemberSelected(user.userId)
    }

    Scaffold(
        topBar = {
            if (!state.memberListState.isSearchActive) {
                RoomMemberListTopBar(onBackPressed = onBackPressed)
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MemberListView(
                state = state.memberListState,
                onUserSelected = ::onUserSelected,
            )

            if (!state.memberListState.isSearchActive) {
                if (state.allUsers is Async.Success) {
                    LazyColumn(modifier = Modifier.fillMaxWidth(), state = rememberLazyListState()) {
                        item {
                            val memberCount = state.allUsers.state.count()
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                text = pluralStringResource(id = R.plurals.screen_room_member_list_header_title, count = memberCount, memberCount),
                                style = ElementTextStyles.Regular.callout,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Start,
                            )
                        }
                        items(state.allUsers.state) { matrixUser ->
                            SearchSingleMemberResultItem(
                                modifier = Modifier.fillMaxWidth(),
                                matrixUser = matrixUser,
                                onClick = { onUserSelected(matrixUser) }
                            )
                        }
                    }
                } else if (state.allUsers.isLoading()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomMemberListTopBar(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.screen_room_details_people_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = { BackButton(onClick = onBackPressed) },
    )
}

@Preview
@Composable
fun RoomMemberListLightPreview(@PreviewParameter(RoomMemberListStateProvider::class) state: RoomMemberListState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun RoomMemberListDarkPreview(@PreviewParameter(RoomMemberListStateProvider::class) state: RoomMemberListState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: RoomMemberListState) {
    RoomMemberListView(state)
}
