/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.element.android.appconfig.RoomListConfig
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.HomeNavigationBarItem
import io.element.android.features.home.impl.filters.RoomListFiltersState
import io.element.android.features.home.impl.filters.RoomListFiltersView
import io.element.android.features.home.impl.filters.aRoomListFiltersState
import io.element.android.features.home.impl.spacefilters.SpaceFiltersEvent
import io.element.android.features.home.impl.spacefilters.SpaceFiltersState
import io.element.android.features.home.impl.spacefilters.aDisabledSpaceFiltersState
import io.element.android.features.home.impl.spacefilters.aSelectedSpaceFiltersState
import io.element.android.features.home.impl.spacefilters.anUnselectedSpaceFiltersState
import io.element.android.libraries.designsystem.atomic.atoms.RedIndicatorAtom
import io.element.android.libraries.designsystem.colors.gradientSubtleColors
import io.element.android.libraries.designsystem.components.TopAppBarScrollBehaviorLayout
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    selectedNavigationItem: HomeNavigationBarItem,
    currentUserAndNeighbors: ImmutableList<MatrixUser>,
    showAvatarIndicator: Boolean,
    areSearchResultsDisplayed: Boolean,
    onToggleSearch: () -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    onOpenSettings: () -> Unit,
    onAccountSwitch: (SessionId) -> Unit,
    onCreateSpace: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    canCreateSpaces: Boolean,
    canReportBug: Boolean,
    displayFilters: Boolean,
    filtersState: RoomListFiltersState,
    spaceFiltersState: SpaceFiltersState,
    modifier: Modifier = Modifier,
) {
    // Determine title scale and alpha based on scroll behavior
    val collapsedFraction = scrollBehavior.state.collapsedFraction
    
    // Wrap entire header with a darker base color
    Box(
        modifier = modifier
    ) {
        // Gradient overlay at the top - fixed height, independent of header size
        if (!areSearchResultsDisplayed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp) // Increase height to cover status bar area
                        .background(
                            brush = Brush.verticalGradient(
                                0.0f to gradientSubtleColors()[0].copy(alpha = 0.2f),
                                0.3f to gradientSubtleColors()[1].copy(alpha = 0.1f),
                                1.0f to Color.Transparent
                            ),
                        )
                        .alpha(1f - collapsedFraction)
                )
        }
        
        Column(
            modifier = Modifier.statusBarsPadding()
        ) {
            // Top Bar with profile and search icons
            // Title is empty because we overlay our single transforming title
            TopAppBar(
                modifier = Modifier,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
                title = {},
                navigationIcon = {
                    NavigationIcon(
                        currentUserAndNeighbors = currentUserAndNeighbors,
                        showAvatarIndicator = showAvatarIndicator,
                        onAccountSwitch = onAccountSwitch,
                        onClick = onOpenSettings,
                    )
                },
                actions = {
                    when (selectedNavigationItem) {
                        HomeNavigationBarItem.Chats -> RoomListMenuItems(
                            onToggleSearch = onToggleSearch,
                            onMenuActionClick = onMenuActionClick,
                            canReportBug = canReportBug
                        )
                        HomeNavigationBarItem.Spaces -> SpacesMenuItems(
                            canCreateSpaces = canCreateSpaces,
                            onCreateSpace = onCreateSpace
                        )
                    }
                },
                windowInsets = WindowInsets(left = 4.dp),
                scrollBehavior = null,
            )

            // Large area for filters and branding space
            TopAppBarScrollBehaviorLayout(scrollBehavior = scrollBehavior) {
                Column {
                    // Space for branding text when expanded
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp) // Height for branding space
                    )

                    // Filters below the branding
                    if (displayFilters) {
                        RoomListFiltersView(
                            state = filtersState,
                            modifier = Modifier
                                .background(Color.Transparent)
                                .padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }

        // --- Shared Branding Text (Transforming) ---
        // We Use a Box that fills the whole header to place the text
        val brandingFontSize = ElementTheme.typography.aliasScreenTitle.fontSize
        val titleScale = 2.0f - (collapsedFraction * 1.0f)
        
        // Translation logic for smooth shared-element feel
        // Expanded: Centered in the 144dp height (approx)
        // Collapsed: Vertically centered next to avatar with proper offset
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
        ) {
            // Calculate smooth values
            val progress = collapsedFraction.coerceIn(0f, 1f)
            
            // X: Always centered horizontally
            val horizontalBias = 0.0f
            
            // Y: Expanded lower (2.0f) to Collapsed centered in top section (-0.5f)
            val verticalBias = 2.0f - (progress * 1.5f)

            Text(
                text = "Funker",
                style = ElementTheme.typography.aliasScreenTitle.copy(
                    fontSize = brandingFontSize * titleScale
                ),
                modifier = Modifier
                    .align(androidx.compose.ui.BiasAlignment(horizontalBias, verticalBias))
                    .semantics { heading() }
            )
        }
    }
}

@Composable
private fun RoomListMenuItems(
    onToggleSearch: () -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    canReportBug: Boolean,
    spaceFiltersState: SpaceFiltersState,
) {
    IconButton(
        onClick = onToggleSearch,
    ) {
        Icon(
            imageVector = CompoundIcons.Search(),
            contentDescription = stringResource(CommonStrings.action_search),
        )
    }
    SpaceFilterButton(spaceFiltersState = spaceFiltersState)
    if (RoomListConfig.HAS_DROP_DOWN_MENU) {
        var showMenu by remember { mutableStateOf(false) }
        IconButton(
            onClick = { showMenu = !showMenu }
        ) {
            Icon(
                imageVector = CompoundIcons.OverflowVertical(),
                contentDescription = null,
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (RoomListConfig.SHOW_INVITE_MENU_ITEM) {
                DropdownMenuItem(
                    onClick = {
                        showMenu = false
                        onMenuActionClick(RoomListMenuAction.InviteFriends)
                    },
                    text = { Text(stringResource(id = CommonStrings.action_invite)) },
                    leadingIcon = {
                        Icon(
                            imageVector = CompoundIcons.ShareAndroid(),
                            tint = ElementTheme.colors.iconSecondary,
                            contentDescription = null,
                        )
                    }
                )
            }
            if (RoomListConfig.SHOW_REPORT_PROBLEM_MENU_ITEM && canReportBug) {
                DropdownMenuItem(
                    onClick = {
                        showMenu = false
                        onMenuActionClick(RoomListMenuAction.ReportBug)
                    },
                    text = { Text(stringResource(id = CommonStrings.common_report_a_problem)) },
                    leadingIcon = {
                        Icon(
                            imageVector = CompoundIcons.ChatProblem(),
                            tint = ElementTheme.colors.iconSecondary,
                            contentDescription = null,
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SpacesMenuItems(
    canCreateSpaces: Boolean,
    onCreateSpace: () -> Unit
) {
    if (canCreateSpaces) {
        IconButton(onClick = onCreateSpace) {
            Icon(
                imageVector = CompoundIcons.Plus(),
                contentDescription = stringResource(CommonStrings.action_create_space)
            )
        }
    }
}

@Composable
private fun SpaceFilterButton(
    spaceFiltersState: SpaceFiltersState,
) {
    when (spaceFiltersState) {
        SpaceFiltersState.Disabled -> Unit
        is SpaceFiltersState.Unselected -> {
            IconButton(
                onClick = { spaceFiltersState.eventSink(SpaceFiltersEvent.Unselected.ShowFilters) }
            ) {
                Icon(
                    imageVector = CompoundIcons.Filter(),
                    contentDescription = null,
                )
            }
        }
        is SpaceFiltersState.Selecting -> {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = CompoundIcons.Filter(),
                    contentDescription = null,
                )
            }
        }
        is SpaceFiltersState.Selected -> {
            IconButton(
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = ElementTheme.colors.bgAccentRest,
                    contentColor = ElementTheme.colors.iconOnSolidPrimary,
                ),
                onClick = { spaceFiltersState.eventSink(SpaceFiltersEvent.Selected.ClearSelection) },
            ) {
                Icon(
                    imageVector = CompoundIcons.Filter(),
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun NavigationIcon(
    currentUserAndNeighbors: ImmutableList<MatrixUser>,
    showAvatarIndicator: Boolean,
    onAccountSwitch: (SessionId) -> Unit,
    onClick: () -> Unit,
) {
    if (currentUserAndNeighbors.size == 1) {
        AccountIcon(
            matrixUser = currentUserAndNeighbors.single(),
            isCurrentAccount = true,
            showAvatarIndicator = showAvatarIndicator,
            onClick = onClick,
        )
    } else {
        // Render a vertical pager
        val pagerState = rememberPagerState(initialPage = 1) { currentUserAndNeighbors.size }
        // Listen to page changes and switch account if needed
        val latestOnAccountSwitch by rememberUpdatedState(onAccountSwitch)
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.settledPage }.collect { page ->
                latestOnAccountSwitch(SessionId(currentUserAndNeighbors[page].userId.value))
            }
        }
        VerticalPager(
            state = pagerState,
            modifier = Modifier.height(48.dp),
        ) { page ->
            AccountIcon(
                matrixUser = currentUserAndNeighbors[page],
                isCurrentAccount = page == 1,
                showAvatarIndicator = page == 1 && showAvatarIndicator,
                onClick = if (page == 1) {
                    onClick
                } else {
                    {}
                },
            )
        }
    }
}

@Composable
private fun AccountIcon(
    matrixUser: MatrixUser,
    isCurrentAccount: Boolean,
    showAvatarIndicator: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val testTag = if (isCurrentAccount) Modifier.testTag(TestTags.homeScreenSettings) else Modifier
    IconButton(
        modifier = modifier.then(testTag),
        onClick = onClick,
    ) {
        Box {
            val avatarData by remember(matrixUser) {
                derivedStateOf {
                    matrixUser.getAvatarData(size = AvatarSize.CurrentUserTopBar)
                }
            }
            Avatar(
                avatarData = avatarData,
                avatarType = AvatarType.User,
                contentDescription = if (isCurrentAccount) stringResource(CommonStrings.common_settings) else null,
            )
            if (showAvatarIndicator) {
                RedIndicatorAtom(
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewsDayNight
@Composable
internal fun HomeTopBarPreview() = ElementPreview {
    HomeTopBar(
        selectedNavigationItem = HomeNavigationBarItem.Chats,
        currentUserAndNeighbors = persistentListOf(MatrixUser(UserId("@id:domain"), "Alice")),
        showAvatarIndicator = false,
        areSearchResultsDisplayed = false,
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
        onOpenSettings = {},
        onAccountSwitch = {},
        onToggleSearch = {},
        onCreateSpace = {},
        canCreateSpaces = true,
        canReportBug = true,
        displayFilters = true,
        filtersState = aRoomListFiltersState(),
        spaceFiltersState = anUnselectedSpaceFiltersState(),
        onMenuActionClick = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewsDayNight
@Composable
internal fun HomeTopBarSpacesPreview() = ElementPreview {
    HomeTopBar(
        selectedNavigationItem = HomeNavigationBarItem.Spaces,
        currentUserAndNeighbors = persistentListOf(MatrixUser(UserId("@id:domain"), "Alice")),
        showAvatarIndicator = false,
        areSearchResultsDisplayed = false,
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
        onOpenSettings = {},
        onAccountSwitch = {},
        onToggleSearch = {},
        onCreateSpace = {},
        canCreateSpaces = true,
        canReportBug = true,
        displayFilters = false,
        filtersState = aRoomListFiltersState(),
        spaceFiltersState = anUnselectedSpaceFiltersState(),
        onMenuActionClick = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewsDayNight
@Composable
internal fun HomeTopBarWithIndicatorPreview() = ElementPreview {
    HomeTopBar(
        selectedNavigationItem = HomeNavigationBarItem.Chats,
        currentUserAndNeighbors = persistentListOf(MatrixUser(UserId("@id:domain"), "Alice")),
        showAvatarIndicator = true,
        areSearchResultsDisplayed = false,
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
        onOpenSettings = {},
        onAccountSwitch = {},
        onToggleSearch = {},
        onCreateSpace = {},
        canCreateSpaces = true,
        canReportBug = true,
        displayFilters = true,
        filtersState = aRoomListFiltersState(),
        spaceFiltersState = anUnselectedSpaceFiltersState(),
        onMenuActionClick = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewsDayNight
@Composable
internal fun HomeTopBarMultiAccountPreview() = ElementPreview {
    HomeTopBar(
        selectedNavigationItem = HomeNavigationBarItem.Chats,
        currentUserAndNeighbors = aMatrixUserList().take(3).toImmutableList(),
        showAvatarIndicator = false,
        areSearchResultsDisplayed = false,
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
        onOpenSettings = {},
        onAccountSwitch = {},
        onToggleSearch = {},
        onCreateSpace = {},
        canCreateSpaces = true,
        canReportBug = true,
        displayFilters = true,
        filtersState = aRoomListFiltersState(),
        spaceFiltersState = anUnselectedSpaceFiltersState(),
        onMenuActionClick = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewsDayNight
@Composable
internal fun HomeTopSpaceFiltersSelectedPreview() = ElementPreview {
    HomeTopBar(
        selectedNavigationItem = HomeNavigationBarItem.Chats,
        currentUserAndNeighbors = persistentListOf(MatrixUser(UserId("@id:domain"), "Alice")),
        showAvatarIndicator = false,
        areSearchResultsDisplayed = false,
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
        onOpenSettings = {},
        onAccountSwitch = {},
        onToggleSearch = {},
        onCreateSpace = {},
        canCreateSpaces = true,
        canReportBug = true,
        displayFilters = true,
        filtersState = aRoomListFiltersState(),
        spaceFiltersState = aSelectedSpaceFiltersState(),
        onMenuActionClick = {},
    )
}
