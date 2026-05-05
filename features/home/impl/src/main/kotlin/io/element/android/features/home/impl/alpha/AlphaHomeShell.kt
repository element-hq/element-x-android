/*
 * Copyright 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.alpha

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.R
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * The Alpha demo replaces Element X's single-screen home with a WeChat-style four-tab
 * shell. Messages keeps the existing HomeView intact (chat list + invites + search). The
 * other three tabs are deliberately bare so we can iterate later without churning chat UX.
 */
@Composable
internal fun AlphaHomeShell(
    onSettingsClick: () -> Unit,
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
                AlphaTab.Contacts -> AlphaComingSoonTab(
                    titleRes = R.string.screen_alpha_tab_contacts,
                )
                AlphaTab.Discover -> AlphaComingSoonTab(
                    titleRes = R.string.screen_alpha_tab_discover,
                )
                AlphaTab.Me -> AlphaMeTab(onSettingsClick = onSettingsClick)
            }
        }
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
private fun AlphaMeTab(onSettingsClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(24.dp))
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_alpha_me_settings)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Settings())),
            onClick = onSettingsClick,
        )
    }
}
