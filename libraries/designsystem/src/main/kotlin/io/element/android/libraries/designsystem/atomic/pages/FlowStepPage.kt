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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar

/**
 * A Page with:
 * - a top bar as TobAppBar with optional back button (displayed if [onBackClicked] is not null)
 * - a header, as IconTitleSubtitleMolecule
 * - a content.
 * - a footer, as ButtonColumnMolecule
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowStepPage(
    iconVector: ImageVector?,
    title: String,
    modifier: Modifier = Modifier,
    onBackClicked: (() -> Unit)? = null,
    subTitle: String? = null,
    content: @Composable () -> Unit = {},
    buttons: @Composable ColumnScope.() -> Unit = {},
) {
    BackHandler(enabled = onBackClicked != null) {
        onBackClicked?.invoke()
    }
    HeaderFooterPage(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onBackClicked != null) {
                        BackButton(onClick = onBackClicked)
                    }
                },
                title = {},
            )
        },
        header = {
            IconTitleSubtitleMolecule(
                iconImageVector = iconVector,
                title = title,
                subTitle = subTitle,
            )
        },
        content = content,
        footer = {
            ButtonColumnMolecule(
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                buttons()
            }
        }
    )
}

@PreviewsDayNight
@Composable
internal fun FlowStepPagePreview() = ElementPreview {
    FlowStepPage(
        onBackClicked = {},
        title = "Title",
        subTitle = "Subtitle",
        iconVector = CompoundIcons.Computer(),
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
        buttons = {
            TextButton(text = "A button", onClick = { })
            Button(text = "Continue", onClick = { })
        }
    )
}
