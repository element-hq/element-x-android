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

package io.element.android.features.roomlist.impl.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun RoomListFiltersView(
    state: RoomListFiltersState,
    modifier: Modifier = Modifier
) {

    fun onClearFiltersClicked() {
        state.eventSink(RoomListFiltersEvents.ClearSelectedFilters)
    }

    fun onFilterClicked(filter: RoomListFilter) {
        state.eventSink(RoomListFiltersEvents.ToggleFilter(filter))
    }

    val horizontalPadding = if (state.showClearFilterButton) 4.dp else 16.dp
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .padding(horizontal = horizontalPadding)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnimatedVisibility(visible = state.showClearFilterButton) {
            RoomListClearFiltersButton(onClick = ::onClearFiltersClicked)
        }
        for (filter in state.selectedFilters) {
            RoomListFilterView(
                roomListFilter = filter,
                selected = true,
                onClick = ::onFilterClicked,
            )
        }
        for (filter in state.unselectedFilters) {
            RoomListFilterView(
                roomListFilter = filter,
                selected = false,
                onClick = ::onFilterClicked,
            )
        }
    }
}

@Composable
private fun RoomListClearFiltersButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(ElementTheme.colors.bgActionPrimaryRest)
        ) {
            Icon(
                modifier = Modifier.align(Alignment.Center),
                imageVector = CompoundIcons.Close(),
                tint = ElementTheme.colors.iconOnSolidPrimary,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun RoomListFilterView(
    roomListFilter: RoomListFilter,
    selected: Boolean,
    onClick: (RoomListFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = { onClick(roomListFilter) },
        modifier = modifier,
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = ElementTheme.colors.bgCanvasDefault,
            selectedContainerColor = ElementTheme.colors.bgActionPrimaryRest,
            labelColor = ElementTheme.colors.textPrimary,
            selectedLabelColor = ElementTheme.colors.textOnSolidPrimary,
        ),
        label = {
            Text(text = stringResource(id = roomListFilter.stringResource))
        }
    )
}

@PreviewsDayNight
@Composable
internal fun RoomListFiltersViewPreview(@PreviewParameter(RoomListFiltersStateProvider::class) state: RoomListFiltersState) = ElementPreview {
    RoomListFiltersView(
        state = state,
    )
}
