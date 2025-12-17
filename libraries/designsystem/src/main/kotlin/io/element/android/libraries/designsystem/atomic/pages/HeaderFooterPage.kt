/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * @param modifier Classical modifier.
 * @param contentPadding padding values to apply to the content.
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
    contentPadding: PaddingValues = PaddingValues(20.dp),
    containerColor: Color = ElementTheme.colors.bgCanvasDefault,
    isScrollable: Boolean = false,
    background: @Composable () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        containerColor = containerColor,
    ) { insetsPadding ->
        val layoutDirection = LocalLayoutDirection.current
        val contentInsetsPadding = remember(insetsPadding, layoutDirection) {
            PaddingValues(
                start = insetsPadding.calculateStartPadding(layoutDirection),
                top = insetsPadding.calculateTopPadding(),
                end = insetsPadding.calculateEndPadding(layoutDirection),
            )
        }
        val footerInsetsPadding = remember(insetsPadding, layoutDirection) {
            PaddingValues(
                start = insetsPadding.calculateStartPadding(layoutDirection),
                end = insetsPadding.calculateEndPadding(layoutDirection),
                bottom = insetsPadding.calculateBottomPadding(),
            )
        }
        Box {
            background()

            // Render in a Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = contentPadding)
                    .consumeWindowInsets(insetsPadding)
                    .imePadding(),
            ) {
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .run {
                            if (isScrollable) {
                                verticalScroll(rememberScrollState())
                                    // Make sure the scrollable content takes the full available height
                                    .height(IntrinsicSize.Max)
                            } else {
                                Modifier
                            }
                        }
                        // Apply insets here so if the content is scrollable it can get below the top app bar if needed
                        .padding(contentInsetsPadding)
                        .weight(1f, fill = true),
                ) {
                    // Header
                    header()
                    Box {
                        content()
                    }
                }

                // Footer
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .fillMaxWidth()
                        .padding(footerInsetsPadding)
                ) {
                    footer()
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
                modifier = Modifier.fillMaxWidth(),
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

@PreviewsDayNight
@Composable
internal fun HeaderFooterPageScrollablePreview() = ElementPreview {
    HeaderFooterPage(
        content = {
            Box(
                modifier = Modifier.fillMaxWidth(),
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
        },
        isScrollable = true,
    )
}
