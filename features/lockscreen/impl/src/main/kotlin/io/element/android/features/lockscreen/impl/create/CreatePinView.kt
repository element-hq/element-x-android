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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.lockscreen.impl.create

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@Composable
fun CreatePinView(
    state: CreatePinState,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackClicked)
                },
                title = {}
            )
        },
        content = { padding ->
            HeaderFooterPage(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
                header = { CreatePinHeader() },
                footer = { CreatePinFooter() },
            )
        }
    )
}

@Composable
private fun CreatePinHeader(
    modifier: Modifier = Modifier,
) {
    IconTitleSubtitleMolecule(
        modifier = modifier,
        title = "Choose 4 digit PIN",
        subTitle = "Lock Element to add extra security to your chats.\n\nChoose something memorable. If you forget this PIN, you will be logged out of the app",
        iconImageVector = Icons.Default.Lock,
    )
}

@Composable
private fun CreatePinFooter() {
    Button(
        modifier = Modifier.fillMaxWidth(),
        text = "Continue",
        onClick = {

        }
    )
}

@Composable
@PreviewsDayNight
internal fun CreatePinViewPreview(@PreviewParameter(CreatePinStateProvider::class) state: CreatePinState) {
    ElementPreview {
        CreatePinView(
            state = state,
            onBackClicked = {},
        )
    }
}
