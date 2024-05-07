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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoomListFiltersView(
    state: RoomListFiltersState,
    modifier: Modifier = Modifier
) {
    fun onClearFiltersClicked() {
        state.eventSink(RoomListFiltersEvents.ClearSelectedFilters)
    }

    fun onToggleFilter(filter: RoomListFilter) {
        state.eventSink(RoomListFiltersEvents.ToggleFilter(filter))
    }

    val lazyListState = rememberLazyListState()
    val previousFilters = remember { mutableStateOf(listOf<RoomListFilter>()) }
    LazyRow(
        contentPadding = PaddingValues(start = 8.dp, end = 16.dp),
        modifier = modifier.fillMaxWidth(),
        state = lazyListState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item("clear_filters") {
            if (state.hasAnyFilterSelected) {
                RoomListClearFiltersButton(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .testTag(TestTags.homeScreenClearFilters),
                    onClick = {
                        previousFilters.value = state.selectedFilters()
                        onClearFiltersClicked()
                    }
                )
            }
        }
        state.filterSelectionStates.forEachIndexed { i, filterWithSelection ->
            item(filterWithSelection.filter) {
                val zIndex = (if (previousFilters.value.contains(filterWithSelection.filter)) state.filterSelectionStates.size else 0) - i.toFloat()
                RoomListFilterView(
                    modifier = Modifier
                        .animateItemPlacement()
                        .zIndex(zIndex),
                    roomListFilter = filterWithSelection.filter,
                    selected = filterWithSelection.isSelected,
                    onClick = {
                        previousFilters.value = state.selectedFilters()
                        onToggleFilter(it)
                    },
                )
            }
        }
    }
    LaunchedEffect(state.filterSelectionStates) {
        // Checking for canScrollBackward is necessary for the itemPlacementAnimation to work correctly.
        // We don't want the itemPlacementAnimation to be triggered when clearing the filters.
        if (!state.hasAnyFilterSelected || lazyListState.canScrollBackward) {
            lazyListState.animateScrollToItem(0)
        }
    }
}

@Composable
private fun RoomListClearFiltersButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(ElementTheme.colors.bgActionPrimaryRest)
            .clickable(onClick = onClick)
    ) {
        Icon(
            modifier = Modifier.align(Alignment.Center),
            imageVector = CompoundIcons.Close(),
            tint = ElementTheme.colors.iconOnSolidPrimary,
            contentDescription = stringResource(id = io.element.android.libraries.ui.strings.R.string.action_clear),
        )
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
        modifier = modifier.height(36.dp),
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
