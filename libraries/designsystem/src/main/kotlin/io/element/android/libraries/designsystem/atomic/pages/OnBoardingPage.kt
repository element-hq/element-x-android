/*
 * Copyright (c) 2023 New Vector Ltd
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.theme.ElementTheme

/**
 * Page for onboarding screens, with content and optional footer.
 *
 * Ref: https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?type=design&node-id=133-5427&t=5SHVppfYzjvkEywR-0
 * @param modifier Classical modifier.
 * @param footer optional footer.
 * @param content main content.
 */
@Composable
fun OnBoardingPage(
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    // Note: having a night variant of R.drawable.onboarding_bg in the folder `drawable-night` is working
    // at runtime, but is not in Android Studio Preview. So I prefer to handle this manually.
    val isLight = ElementTheme.colors.isLight
    val bgDrawableRes = if (isLight) R.drawable.onboarding_bg_light else R.drawable.onboarding_bg_dark
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // BG
        Image(
            modifier = Modifier
                .fillMaxSize(),
            painter = painterResource(id = bgDrawableRes),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(vertical = 16.dp),
        ) {
            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
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

@Preview
@Composable
internal fun OnBoardingPageLightPreview() {
    ElementPreview { ContentToPreview() }
}

@Preview
@Composable
internal fun OnBoardingPageDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
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
