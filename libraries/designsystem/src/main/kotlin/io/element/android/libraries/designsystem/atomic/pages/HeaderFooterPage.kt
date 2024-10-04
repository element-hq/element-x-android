/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * @param modifier Classical modifier.
 * @param paddingValues padding values to apply to the content.
 * @param containerColor color of the container. Set to [Color.Transparent] if you provide a background in the [modifier].
 * @param isScrollable if the whole content should be scrollable.
 * @param background optional background component.
 * @param topBar optional topBar.
 * @param header optional header.
 * @param footer optional footer.
 * @param content main content.
 */
@Suppress("NAME_SHADOWING")
@Composable
fun HeaderFooterPage(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(20.dp),
    containerColor: Color = MaterialTheme.colorScheme.background,
    isScrollable: Boolean = false,
    background: @Composable () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val topBar = remember { movableContentOf(topBar) }
    val header = remember { movableContentOf(header) }
    val footer = remember { movableContentOf(footer) }
    val content = remember { movableContentOf(content) }
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        containerColor = containerColor,
    ) { padding ->
        Box {
            background()
            if (isScrollable) {
                // Render in a LazyColumn
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues = paddingValues)
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .imePadding()
                ) {
                    // Header
                    item {
                        header()
                    }
                    // Content
                    item {
                        content()
                    }
                    // Footer
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            footer()
                        }
                    }
                }
            } else {
                // Render in a Column
                Column(
                    modifier = Modifier
                        .padding(paddingValues = paddingValues)
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .imePadding()
                ) {
                    // Header
                    header()
                    // Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        content()
                    }
                    // Footer
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        footer()
                    }
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun HeaderFooterPagePreview() = ElementPreview {
    HeaderFooterPage(
        content = {
            Box(
                Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Content",
                    style = ElementTheme.typography.fontHeadingXlBold
                )
            }
        },
        header = {
            Box(
                Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Header",
                    style = ElementTheme.typography.fontHeadingXlBold
                )
            }
        },
        footer = {
            Box(
                Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Footer",
                    style = ElementTheme.typography.fontHeadingXlBold
                )
            }
        }
    )
}
