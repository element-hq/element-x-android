/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * Page for onboarding screens, with content and optional footer.
 *
 * Ref: https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?type=design&node-id=133-5427&t=5SHVppfYzjvkEywR-0
 * @param modifier Classical modifier.
 * @param renderBackground whether to render the background image or not.
 * @param contentAlignment horizontal alignment of the contents.
 * @param footer optional footer.
 * @param content main content.
 */
@Composable
fun OnBoardingPage(
    modifier: Modifier = Modifier,
    renderBackground: Boolean = true,
    contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    footer: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // BG
        if (renderBackground) {
            Image(
                modifier = Modifier
                    .fillMaxSize(),
                painter = painterResource(id = R.drawable.onboarding_bg),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(all = 20.dp),
        ) {
            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = contentAlignment,
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

@PreviewsDayNight
@Composable
internal fun OnBoardingPagePreview() = ElementPreview {
    OnBoardingPage(
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
