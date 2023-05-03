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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.roomlist.impl.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.features.roomlist.impl.R
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.MediumTopAppBar
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListTopBar(
    matrixUser: MatrixUser?,
    areSearchResultsDisplayed: Boolean,
    onFilterChanged: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    LogCompositions(
        tag = "RoomListScreen",
        msg = "TopBar"
    )

    fun closeFilter() {
        onFilterChanged("")
    }

    BackHandler(enabled = areSearchResultsDisplayed) {
        closeFilter()
        onToggleSearch()
    }

    DefaultRoomListTopBar(
        matrixUser = matrixUser,
        onOpenSettings = onOpenSettings,
        onSearchClicked = onToggleSearch,
        scrollBehavior = scrollBehavior,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultRoomListTopBar(
    matrixUser: MatrixUser?,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    onSearchClicked: () -> Unit = {},
) {
    MediumTopAppBar(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        title = {
            Text(
                fontWeight = FontWeight.Bold,
                text = stringResource(id = R.string.screen_roomlist_main_space_title)
            )
        },
        navigationIcon = {
            if (matrixUser != null) {
                IconButton(
                    modifier = Modifier.testTag(TestTags.homeScreenSettings),
                    onClick = onOpenSettings
                ) {
                    Avatar(matrixUser.getAvatarData(), contentDescription = stringResource(StringR.string.common_settings))
                }
            }
        },
        actions = {
            IconButton(
                onClick = onSearchClicked,
            ) {
                Icon(Icons.Default.Search, contentDescription = stringResource(StringR.string.action_search))
            }
        },
        scrollBehavior = scrollBehavior,
        windowInsets = WindowInsets(0.dp),
    )
}

@Preview
@Composable
internal fun DefaultRoomListTopBarLightPreview() = ElementPreviewLight { DefaultRoomListTopBarPreview() }

@Preview
@Composable
internal fun DefaultRoomListTopBarDarkPreview() = ElementPreviewDark { DefaultRoomListTopBarPreview() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultRoomListTopBarPreview() {
    DefaultRoomListTopBar(
        matrixUser = MatrixUser(UserId("@id:domain"), "Alice"),
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState()),
    )
}
