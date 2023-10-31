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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.theme.ElementTheme

/**
 * @param modifier Classical modifier.
 * @param topBar optional topBar.
 * @param header optional header.
 * @param footer optional footer.
 * @param content main content.
 */
@Composable
fun HeaderFooterPage(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .padding(all = 20.dp),
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
            footer()
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
