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

package io.element.android.features.lockscreen.impl.setup.biometric

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.TextButton

@Composable
fun SetupBiometricView(
    state: SetupBiometricState,
    modifier: Modifier = Modifier,
) {
    BackHandler(true) {
        state.eventSink(SetupBiometricEvents.Skip)
    }
    HeaderFooterPage(
        modifier = modifier.padding(top = 80.dp),
        header = {
            SetupBiometricHeader()
        },
        footer = {
            SetupBiometricFooter(
                onAllowClicked = { state.eventSink(SetupBiometricEvents.Allow) },
                onSkipClicked = { state.eventSink(SetupBiometricEvents.Skip) }
            )
        },
    )
}

@Composable
private fun SetupBiometricHeader(modifier: Modifier = Modifier) {
    IconTitleSubtitleMolecule(
        iconImageVector = Icons.Default.Fingerprint,
        title = "Allow biometric unlock",
        subTitle = "Save yourself some time and use biometric authentication to unlock the app each time",
        modifier = modifier
    )
}

@Composable
private fun SetupBiometricFooter(
    onAllowClicked: () -> Unit,
    onSkipClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(text = "Allow biometric unlock", onClick = onAllowClicked)
        TextButton(text = "I'd rather use PIN", onClick = onSkipClicked)
    }
}

@Composable
@PreviewsDayNight
internal fun SetupBiometricViewPreview(@PreviewParameter(SetupBiometricStateProvider::class) state: SetupBiometricState) {
    ElementPreview {
        SetupBiometricView(
            state = state,
        )
    }
}

