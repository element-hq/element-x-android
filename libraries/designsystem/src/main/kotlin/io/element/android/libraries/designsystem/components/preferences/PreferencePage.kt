/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@Composable
fun PreferencePage(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            PreferenceTopAppBar(
                title = title,
                onBackClick = onBackClick,
            )
        },
        snackbarHost = snackbarHost,
        content = {
            Column(
                modifier = Modifier
                    .padding(it)
                    .consumeWindowInsets(it)
                    .verticalScroll(state = rememberScrollState())
            ) {
                content()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferenceTopAppBar(
    title: String,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
            Text(
                modifier = Modifier.semantics {
                    heading()
                },
                text = title,
                style = ElementTheme.typography.aliasScreenTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@PreviewsDayNight
@Composable
internal fun PreferencePagePreview() = ElementPreview {
    PreferencePage(
        title = "Preference screen",
        onBackClick = {},
    ) {
        PreferenceCategory(
            title = "Category title",
        ) {
            PreferenceDivider()
            PreferenceSwitch(
                title = "Switch",
                icon = CompoundIcons.Threads(),
                isChecked = true,
                onCheckedChange = {},
            )
            PreferenceDivider()
            PreferenceCheckbox(
                title = "Checkbox",
                icon = CompoundIcons.Notifications(),
                isChecked = true,
                onCheckedChange = {},
            )
            PreferenceDivider()
            PreferenceSlide(
                title = "Slide",
                summary = "Summary",
                value = 0.75F,
                showIconAreaIfNoIcon = true,
                onValueChange = {},
            )
        }
    }
}
