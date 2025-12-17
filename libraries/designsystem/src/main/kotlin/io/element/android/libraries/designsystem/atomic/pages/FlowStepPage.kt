/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar

/**
 * A Page with:
 * - a top bar as TobAppBar with optional back button (displayed if [onBackClick] is not null)
 * - a header, as IconTitleSubtitleMolecule
 * - a content.
 * - a footer, as ButtonColumnMolecule
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowStepPage(
    iconStyle: BigIcon.Style,
    title: String,
    modifier: Modifier = Modifier,
    isScrollable: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    subTitle: String? = null,
    buttons: @Composable ColumnScope.() -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    BackHandler(enabled = onBackClick != null) {
        onBackClick?.invoke()
    }
    HeaderFooterPage(
        modifier = modifier,
        isScrollable = isScrollable,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onBackClick != null) {
                        BackButton(onClick = onBackClick)
                    }
                },
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        header = {
            IconTitleSubtitleMolecule(
                modifier = Modifier.padding(bottom = 16.dp),
                title = title,
                subTitle = subTitle,
                iconStyle = iconStyle,
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
        onBackClick = {},
        title = "Title",
        subTitle = "Subtitle",
        iconStyle = BigIcon.Style.Default(CompoundIcons.Computer()),
        buttons = {
            TextButton(text = "A button", onClick = { })
            Button(text = "Continue", onClick = { })
        }
    ) {
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
    }
}
