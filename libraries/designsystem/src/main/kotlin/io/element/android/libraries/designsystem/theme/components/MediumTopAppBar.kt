/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediumTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.mediumTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    androidx.compose.material3.MediumTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = {
            CompositionLocalProvider(LocalContentColor provides ElementTheme.colors.textActionPrimary) {
                actions()
            }
        },
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(group = PreviewGroup.AppBars)
@Composable
internal fun MediumTopAppBarPreview() = ElementThemedPreview {
    MediumTopAppBar(
        title = { Text(text = "Title") },
        navigationIcon = { BackButton(onClick = {}) },
        actions = {
            TextButton(text = "Action", onClick = {})
            IconButton(onClick = {}) {
                Icon(
                    imageVector = CompoundIcons.ShareAndroid(),
                    contentDescription = null,
                )
            }
        }
    )
}
