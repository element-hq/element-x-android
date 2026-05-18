/*
 * Copyright 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.alpha

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.R
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserHeader
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The Alpha demo replaces Element X's single-screen home with a WeChat-style four-tab
 * shell. Messages keeps the existing HomeView intact (chat list + invites + search). The
 * other three tabs are deliberately bare so we can iterate later without churning chat UX.
 */
@Composable
internal fun AlphaHomeShell(
    onSettingsClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onRoomClick: (RoomId) -> Unit,
    currentUser: MatrixUser?,
    roomSummaries: ImmutableList<RoomListRoomSummary> = persistentListOf(),
    modifier: Modifier = Modifier,
    homeContent: @Composable () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(AlphaTab.Messages) }
    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = ElementTheme.colors.bgCanvasDefault,
            ) {
                AlphaTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon(),
                                contentDescription = stringResource(tab.labelRes),
                            )
                        },
                        label = { Text(stringResource(tab.labelRes)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElementTheme.colors.iconPrimary,
                            selectedTextColor = ElementTheme.colors.textPrimary,
                            unselectedIconColor = ElementTheme.colors.iconSecondary,
                            unselectedTextColor = ElementTheme.colors.textSecondary,
                            indicatorColor = ElementTheme.colors.bgSubtleSecondary,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (selectedTab) {
                AlphaTab.Messages -> homeContent()
                AlphaTab.Contacts -> AlphaContactsTab(
                    summaries = roomSummaries,
                    onRoomClick = onRoomClick,
                )
                AlphaTab.Discover -> AlphaComingSoonTab(
                    titleRes = R.string.screen_alpha_tab_discover,
                )
                AlphaTab.Me -> AlphaMeTab(
                    currentUser = currentUser,
                    onSettingsClick = onSettingsClick,
                    onSignOutClick = onSignOutClick,
                )
            }
        }
    }
}

@Composable
private fun AlphaContactsTab(
    summaries: ImmutableList<RoomListRoomSummary>,
    onRoomClick: (RoomId) -> Unit,
) {
    val dms = remember(summaries) { summaries.filter { it.isDm } }
    val groups = remember(summaries) { summaries.filter { !it.isDm && !it.isSpace } }
    if (dms.isEmpty() && groups.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.screen_alpha_contacts_empty),
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (dms.isNotEmpty()) {
            item { AlphaContactsSectionHeader(R.string.screen_alpha_contacts_section_dms, dms.size) }
            items(dms, key = { it.roomId.value }) { room ->
                AlphaContactRow(room = room, onClick = { onRoomClick(room.roomId) })
            }
        }
        if (groups.isNotEmpty()) {
            item { AlphaContactsSectionHeader(R.string.screen_alpha_contacts_section_groups, groups.size) }
            items(groups, key = { it.roomId.value }) { room ->
                AlphaContactRow(room = room, onClick = { onRoomClick(room.roomId) })
            }
        }
    }
}

@Composable
private fun AlphaContactsSectionHeader(titleRes: Int, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            style = ElementTheme.typography.fontBodyMdMedium,
            color = ElementTheme.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = count.toString(),
            style = ElementTheme.typography.fontBodySmRegular,
            color = ElementTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun AlphaContactRow(
    room: RoomListRoomSummary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(40.dp)) {
            Avatar(
                avatarData = room.avatarData,
                avatarType = if (room.isDm) {
                    AvatarType.User
                } else {
                    AvatarType.Room(heroes = room.heroes, isTombstoned = room.isTombstoned)
                },
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = room.name.orEmpty().ifBlank { "" },
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AlphaComingSoonTab(titleRes: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(titleRes),
                style = ElementTheme.typography.fontHeadingMdBold,
                color = ElementTheme.colors.textPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.screen_alpha_tab_coming_soon),
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AlphaMeTab(
    currentUser: MatrixUser?,
    onSettingsClick: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(16.dp))
        if (currentUser != null) {
            MatrixUserHeader(matrixUser = currentUser)
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_alpha_me_settings)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Settings())),
            onClick = onSettingsClick,
        )
        // Sign out lives directly on the Me tab so a casual user doesn't have to dig
        // into Settings to find Element's "Remove this device" entry.
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_alpha_me_signout)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Close())),
            style = ListItemStyle.Destructive,
            onClick = onSignOutClick,
        )
    }
}
