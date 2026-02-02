/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spacefilters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.SearchField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.spaces.SpaceServiceFilter
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceFiltersView(
    state: SpaceFiltersState,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        when (state) {
            is SpaceFiltersState.Selecting -> showSheet = true
            else -> {
                sheetState.hide()
                showSheet = false
            }
        }
    }

    Box(modifier = modifier) {
        if (showSheet && state is SpaceFiltersState.Selecting) {
            ModalBottomSheet(
                modifier = Modifier
                    .systemBarsPadding()
                    .navigationBarsPadding(),
                sheetState = sheetState,
                onDismissRequest = { state.eventSink(SpaceFiltersEvent.Selecting.Cancel) },
            ) {
                SpaceFiltersBottomSheetContent(
                    filters = state.visibleFilters,
                    searchQuery = state.searchQuery,
                    onFilterSelected = { filter ->
                        state.eventSink(SpaceFiltersEvent.Selecting.SelectFilter(filter))
                    }
                )
            }
        }
    }
}

@Composable
private fun SpaceFiltersBottomSheetContent(
    filters: List<SpaceServiceFilter>,
    searchQuery: TextFieldState,
    onFilterSelected: (SpaceServiceFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "Your spaces",
            style = ElementTheme.typography.fontHeadingSmMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SearchField(
            state = searchQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(CommonStrings.action_search),
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(filters) { filter ->
                SpaceFilterItem(
                    filter = filter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
    }
}

@Composable
private fun SpaceFilterItem(
    filter: SpaceServiceFilter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spaceRoom = filter.spaceRoom
    val supportingText = spaceRoom.canonicalAlias?.value

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Level-based indentation
        Spacer(modifier = Modifier.width((16 * filter.level).dp))
        Avatar(
            avatarData = spaceRoom.getAvatarData(AvatarSize.RoomSelectRoomListItem),
            avatarType = AvatarType.Space(),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = spaceRoom.displayName,
                style = ElementTheme.typography.fontBodyLgMedium,
                color = ElementTheme.colors.textPrimary,
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SpaceFiltersViewPreview(@PreviewParameter(SpaceFiltersStateProvider::class) state: SpaceFiltersState) = ElementPreview {
    SpaceFiltersView(state = state)
}
