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

package io.element.android.features.lockscreen.impl.auth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Surface

@Composable
fun PinAuthenticationView(
    state: PinAuthenticationState,
    modifier: Modifier = Modifier,
) {
    Surface(modifier) {
        HeaderFooterPage(
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize(),
            header = { PinAuthenticationHeader(modifier = Modifier.padding(top = 60.dp, bottom = 12.dp)) },
            footer = { PinAuthenticationFooter(state) },
        )
    }
}

@Composable
private fun PinAuthenticationHeader(
    modifier: Modifier = Modifier,
) {
    IconTitleSubtitleMolecule(
        modifier = modifier,
        title = "Element X is locked",
        subTitle = null,
        iconImageVector = Icons.Default.Lock,
    )
}

@Composable
private fun PinAuthenticationFooter(state: PinAuthenticationState) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        text = "Unlock",
        onClick = {
            state.eventSink(PinAuthenticationEvents.Unlock)
        }
    )
}

@Composable
@PreviewsDayNight
internal fun PinAuthenticationViewPreview(@PreviewParameter(PinAuthenticationStateProvider::class) state: PinAuthenticationState) {
    ElementPreview {
        PinAuthenticationView(
            state = state,
        )
    }
}

