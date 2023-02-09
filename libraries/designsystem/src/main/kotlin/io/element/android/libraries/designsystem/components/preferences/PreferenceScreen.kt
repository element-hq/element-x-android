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

package io.element.android.libraries.designsystem.components.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewDefaults
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceView(
    title: String,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
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
                onBackPressed = onBackPressed,
            )
        },
        content = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(it)
                    .verticalScroll(
                        state = scrollState,
                    )
            ) {
                content()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

    )
}

@PreviewDefaults
@Composable
internal fun PreferenceViewPreview() = ElementPreview {
    PreferenceView(
        title = "Preference screen"
    ) {
        PreferenceCategory(
            title = "Category title",
        ) {
            PreferenceText(
                title = "Title",
                icon = Icons.Default.BugReport,
            )
            PreferenceSwitch(
                title = "Switch",
                icon = Icons.Default.Announcement,
                isChecked = true
            )
            PreferenceSlide(
                title = "Slide",
                summary = "Summary",
                value = 0.75F
            )
        }
    }
}
