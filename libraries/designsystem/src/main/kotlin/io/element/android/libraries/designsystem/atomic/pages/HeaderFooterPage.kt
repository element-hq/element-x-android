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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
 * @param background optional background component.
 * @param topBar optional topBar.
 * @param header optional header.
 * @param footer optional footer.
 * @param content main content.
 */
@Composable
fun HeaderFooterPage(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(20.dp),
    containerColor: Color = MaterialTheme.colorScheme.background,
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
    ) { padding ->
        Box {
            background()
            Column(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .padding(padding)
                    .consumeWindowInsets(padding)
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
