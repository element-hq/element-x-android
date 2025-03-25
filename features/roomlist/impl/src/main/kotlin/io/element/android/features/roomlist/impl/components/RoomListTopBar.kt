/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.element.android.appconfig.RoomListConfig
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomlist.impl.R
import io.element.android.features.roomlist.impl.filters.RoomListFiltersState
import io.element.android.features.roomlist.impl.filters.RoomListFiltersView
import io.element.android.features.roomlist.impl.filters.aRoomListFiltersState
import io.element.android.libraries.designsystem.atomic.atoms.RedIndicatorAtom
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatarBloom
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.applyScaleDown
import io.element.android.libraries.designsystem.text.roundToPx
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.MediumTopAppBar
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

private val avatarBloomSize = 430.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListTopBar(
    matrixUser: MatrixUser,
    showAvatarIndicator: Boolean,
    areSearchResultsDisplayed: Boolean,
    onToggleSearch: () -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    onOpenSettings: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    displayMenuItems: Boolean,
    displayFilters: Boolean,
    filtersState: RoomListFiltersState,
    canReportBug: Boolean,
    modifier: Modifier = Modifier,
) {
    DefaultRoomListTopBar(
        matrixUser = matrixUser,
        showAvatarIndicator = showAvatarIndicator,
        areSearchResultsDisplayed = areSearchResultsDisplayed,
        onOpenSettings = onOpenSettings,
        onSearchClick = onToggleSearch,
        onMenuActionClick = onMenuActionClick,
        scrollBehavior = scrollBehavior,
        displayMenuItems = displayMenuItems,
        displayFilters = displayFilters,
        filtersState = filtersState,
        canReportBug = canReportBug,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultRoomListTopBar(
    matrixUser: MatrixUser,
    showAvatarIndicator: Boolean,
    areSearchResultsDisplayed: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onOpenSettings: () -> Unit,
    onSearchClick: () -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    displayMenuItems: Boolean,
    displayFilters: Boolean,
    filtersState: RoomListFiltersState,
    canReportBug: Boolean,
    modifier: Modifier = Modifier,
) {
    // We need this to manually clip the top app bar in preview mode
    val previewAppBarHeight = if (LocalInspectionMode.current) {
        112.dp.roundToPx()
    } else {
        null
    }
    val collapsedFraction = scrollBehavior.state.collapsedFraction
    var appBarHeight by remember {
        mutableIntStateOf(previewAppBarHeight ?: 0)
    }

    val avatarData by remember(matrixUser) {
        derivedStateOf {
            matrixUser.getAvatarData(size = AvatarSize.CurrentUserTopBar)
        }
    }

    val statusBarPadding = with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() }

    Box(modifier = modifier) {
        val collapsedTitleTextStyle = ElementTheme.typography.aliasScreenTitle
        val expandedTitleTextStyle = ElementTheme.typography.fontHeadingLgBold.copy(
            // Due to a limitation of MediumTopAppBar, and to avoid the text to be truncated,
            // ensure that the font size will never be bigger than 28.dp.
            fontSize = 28.dp.applyScaleDown().toSp()
        )
        MaterialTheme(
            colorScheme = ElementTheme.materialColors,
            shapes = MaterialTheme.shapes,
            typography = ElementTheme.materialTypography.copy(
                headlineSmall = expandedTitleTextStyle,
                titleLarge = collapsedTitleTextStyle
            ),
        ) {
            Column(
                modifier = Modifier
                    .onSizeChanged {
                        appBarHeight = it.height
                    }
                    .avatarBloom(
                        avatarData = avatarData,
                        background = if (ElementTheme.isLightTheme) {
                            // Workaround to display a very subtle bloom for avatars with very soft colors
                            Color(0xFFF9F9F9)
                        } else {
                            ElementTheme.colors.bgCanvasDefault
                        },
                        blurSize = DpSize(avatarBloomSize, avatarBloomSize),
                        offset = DpOffset(24.dp, 24.dp + statusBarPadding),
                        clipToSize = if (appBarHeight > 0) {
                            DpSize(
                                avatarBloomSize,
                                appBarHeight.toDp()
                            )
                        } else {
                            DpSize.Unspecified
                        },
                        bottomSoftEdgeColor = ElementTheme.colors.bgCanvasDefault,
                        bottomSoftEdgeAlpha = if (displayFilters) {
                            1f
                        } else {
                            1f - collapsedFraction
                        },
                        alpha = if (areSearchResultsDisplayed) 0f else 1f,
                    )
                    .statusBarsPadding(),
            ) {
                MediumTopAppBar(
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                    title = {
                        Text(text = stringResource(id = R.string.screen_roomlist_main_space_title))
                    },
                    navigationIcon = {
                        NavigationIcon(
                            avatarData = avatarData,
                            showAvatarIndicator = showAvatarIndicator,
                            onClick = onOpenSettings,
                        )
                    },
                    actions = {
                        if (displayMenuItems) {
                            IconButton(
                                onClick = onSearchClick,
                            ) {
                                Icon(
                                    imageVector = CompoundIcons.Search(),
                                    contentDescription = stringResource(CommonStrings.action_search),
                                )
                            }
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
                    },
                    scrollBehavior = scrollBehavior,
                    windowInsets = WindowInsets(0.dp),
                )
                if (displayFilters) {
                    RoomListFiltersView(
                        state = filtersState,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(collapsedFraction)
                .align(Alignment.BottomCenter),
            color = ElementTheme.materialColors.outlineVariant,
        )
    }
}

@Composable
private fun NavigationIcon(
    avatarData: AvatarData,
    showAvatarIndicator: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier.testTag(TestTags.homeScreenSettings),
        onClick = onClick,
    ) {
        Box {
            Avatar(
                avatarData = avatarData,
                contentDescription = stringResource(CommonStrings.common_settings),
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
internal fun DefaultRoomListTopBarPreview() = ElementPreview {
    DefaultRoomListTopBar(
        matrixUser = MatrixUser(UserId("@id:domain"), "Alice"),
        showAvatarIndicator = false,
        areSearchResultsDisplayed = false,
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState()),
        onOpenSettings = {},
        onSearchClick = {},
        displayMenuItems = true,
        displayFilters = true,
        filtersState = aRoomListFiltersState(),
        canReportBug = true,
        onMenuActionClick = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewsDayNight
@Composable
internal fun DefaultRoomListTopBarWithIndicatorPreview() = ElementPreview {
    DefaultRoomListTopBar(
        matrixUser = MatrixUser(UserId("@id:domain"), "Alice"),
        showAvatarIndicator = true,
        areSearchResultsDisplayed = false,
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState()),
        onOpenSettings = {},
        onSearchClick = {},
        displayMenuItems = true,
        displayFilters = true,
        filtersState = aRoomListFiltersState(),
        canReportBug = true,
        onMenuActionClick = {},
    )
}
