/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spacefilters

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.R
import io.element.android.features.home.impl.filters.RoomListFilter
import io.element.android.features.home.impl.filters.RoomListFiltersEvent
import io.element.android.features.home.impl.filters.RoomListFiltersState
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.SearchField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.spaces.SpaceServiceFilter
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceFiltersView(
    state: SpaceFiltersState,
    modifier: Modifier = Modifier,
    roomFiltersState: RoomListFiltersState? = null,
) {
    val isSelecting by rememberUpdatedState(state is SpaceFiltersState.Selecting)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValueTarget ->
            when (sheetValueTarget) {
                SheetValue.Expanded -> isSelecting
                else -> true
            }
        }
    )
    LaunchedEffect(isSelecting) {
        if (!isSelecting) {
            sheetState.hide()
        }
    }
    if (sheetState.isVisible || isSelecting) {
        ModalBottomSheet(
            modifier = modifier
                .systemBarsPadding()
                .navigationBarsPadding(),
            sheetState = sheetState,
            onDismissRequest = {
                if (state is SpaceFiltersState.Selecting) {
                    state.eventSink(SpaceFiltersEvent.Selecting.Cancel)
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                if (state is SpaceFiltersState.Selecting) {
                    CombinedFiltersBottomSheetContent(
                        roomFiltersState = roomFiltersState,
                        spaceFilters = state.visibleFilters,
                        searchQuery = state.searchQuery,
                        onRoomFilterSelected = { filter ->
                            roomFiltersState?.eventSink(RoomListFiltersEvent.ToggleFilter(filter))
                        },
                        onClearRoomFilters = {
                            roomFiltersState?.eventSink(RoomListFiltersEvent.ClearSelectedFilters)
                        },
                        onSpaceFilterSelected = { filter ->
                            state.eventSink(SpaceFiltersEvent.Selecting.SelectFilter(filter))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CombinedFiltersBottomSheetContent(
    roomFiltersState: RoomListFiltersState?,
    spaceFilters: List<SpaceServiceFilter>,
    searchQuery: TextFieldState,
    onRoomFilterSelected: (RoomListFilter) -> Unit,
    onClearRoomFilters: () -> Unit,
    onSpaceFilterSelected: (SpaceServiceFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 16.dp)
    ) {
        if (roomFiltersState != null) {
            RoomFiltersChips(
                filtersState = roomFiltersState,
                onFilterSelected = onRoomFilterSelected,
                onClearFilters = onClearRoomFilters,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = stringResource(R.string.screen_roomlist_your_spaces),
            style = ElementTheme.typography.fontHeadingSmMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SearchField(
            state = searchQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = stringResource(CommonStrings.action_search),
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(spaceFilters) { filter ->
                SpaceFilterItem(
                    filter = filter,
                    onClick = { onSpaceFilterSelected(filter) }
                )
            }
        }
    }
}

@Composable
private fun RoomFiltersChips(
    filtersState: RoomListFiltersState,
    onFilterSelected: (RoomListFilter) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scrollToStart by remember { mutableIntStateOf(0) }
    val lazyListState = rememberLazyListState()
    LaunchedEffect(scrollToStart) {
        while (lazyListState.firstVisibleItemIndex > 0) {
            lazyListState.animateScrollBy(
                value = -(lazyListState.firstVisibleItemScrollOffset + 1f),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
        }
        lazyListState.animateScrollBy(
            value = -lazyListState.firstVisibleItemScrollOffset.toFloat(),
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
    }
    val previousFilters = remember { mutableStateOf(listOf<RoomListFilter>()) }

    LazyRow(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
        modifier = modifier.fillMaxWidth(),
        state = lazyListState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item("clear_filters") {
            if (filtersState.hasAnyFilterSelected) {
                RoomListClearFiltersButton(
                    modifier = Modifier.testTag(TestTags.homeScreenClearFilters),
                    onClick = {
                        previousFilters.value = filtersState.selectedFilters().toList()
                        onClearFilters()
                        scrollToStart++
                    }
                )
            }
        }
        filtersState.filterSelectionStates.forEachIndexed { i, filterWithSelection ->
            item(filterWithSelection.filter) {
                val zIndex = (if (previousFilters.value.contains(filterWithSelection.filter)) filtersState.filterSelectionStates.size else 0) - i.toFloat()
                RoomListFilterChip(
                    modifier = Modifier
                        .animateItem()
                        .zIndex(zIndex),
                    roomListFilter = filterWithSelection.filter,
                    selected = filterWithSelection.isSelected,
                    onClick = {
                        previousFilters.value = filtersState.selectedFilters().toList()
                        onFilterSelected(it)
                        if (filterWithSelection.isSelected.not()) {
                            scrollToStart++
                        }
                    },
                )
            }
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
            .padding(4.dp)
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.Center)
                .size(16.dp),
            imageVector = CompoundIcons.Close(),
            tint = ElementTheme.colors.iconOnSolidPrimary,
            contentDescription = stringResource(id = R.string.screen_roomlist_clear_filters),
        )
    }
}

@Composable
private fun RoomListFilterChip(
    roomListFilter: RoomListFilter,
    selected: Boolean,
    onClick: (RoomListFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val background = animateColorAsState(
        targetValue = if (selected) ElementTheme.colors.bgActionPrimaryRest else ElementTheme.colors.bgCanvasDefault,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chip background colour",
    )
    val textColour = animateColorAsState(
        targetValue = if (selected) ElementTheme.colors.textOnSolidPrimary else ElementTheme.colors.textPrimary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chip text colour",
    )
    val borderColour = animateColorAsState(
        targetValue = if (selected) Color.Transparent else ElementTheme.colors.borderInteractiveSecondary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chip border colour",
    )

    FilterChip(
        selected = selected,
        onClick = { onClick(roomListFilter) },
        modifier = modifier.height(40.dp),
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = background.value,
            selectedContainerColor = background.value,
            labelColor = textColour.value,
            selectedLabelColor = textColour.value,
        ),
        label = {
            Text(
                text = stringResource(id = roomListFilter.stringResource),
                style = ElementTheme.typography.fontBodyMdRegular,
            )
        },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = borderColour.value,
        ),
    )
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
