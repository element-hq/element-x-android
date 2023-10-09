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

package io.element.android.appnav.signedout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.organisms.InfoListItem
import io.element.android.libraries.designsystem.atomic.organisms.InfoListOrganism
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.temporaryColorBgSpecial
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.theme.ElementTheme
import kotlinx.collections.immutable.persistentListOf

// TODO i18n, when wording has been approved.
@Composable
fun SignedOutView(
    state: SignedOutState,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = { state.eventSink(SignedOutEvents.SignInAgain) })
    HeaderFooterPage(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        header = { SignedOutHeader() },
        content = { SignedOutContent() },
        footer = {
            SignedOutFooter(
                onSignInAgain = { state.eventSink(SignedOutEvents.SignInAgain) },
            )
        }
    )
}

@Composable
fun SignedOutHeader() {
    IconTitleSubtitleMolecule(
        modifier = Modifier.padding(top = 60.dp, bottom = 12.dp),
        title = "You’re signed out",
        subTitle = "It can be due to various reasons:",
        iconImageVector = Icons.Filled.AccountCircle
    )
}

@Composable
private fun SignedOutContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = BiasAlignment(
            horizontalBias = 0f,
            verticalBias = -0.4f
        )
    ) {
        InfoListOrganism(
            items = persistentListOf(
                InfoListItem(
                    message = "You’ve changed your password on another session.",
                    iconComposable = { CheckIcon() },
                ),
                InfoListItem(
                    message = "You have deleted this session from another session.",
                    iconComposable = { CheckIcon() },
                ),
                InfoListItem(
                    message = "The administrator of your server has invalidated your access for security reason.",
                    iconComposable = { CheckIcon() },
                ),
            ),
            textStyle = ElementTheme.typography.fontBodyMdMedium,
            iconTint = ElementTheme.colors.textPrimary,
            backgroundColor = ElementTheme.colors.temporaryColorBgSpecial
        )
    }
}

@Composable
private fun CheckIcon(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier
            .size(20.dp)
            .background(color = MaterialTheme.colorScheme.background, shape = CircleShape)
            .padding(2.dp),
        resourceId = CommonDrawables.ic_compound_check,
        contentDescription = null,
        tint = ElementTheme.colors.textActionAccent,
    )
}

@Composable
private fun SignedOutFooter(
    modifier: Modifier = Modifier,
    onSignInAgain: () -> Unit,
) {
    ButtonColumnMolecule(
        modifier = modifier,
    ) {
        Button(
            text = "Sign in again",
            onClick = onSignInAgain,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewsDayNight
@Composable
fun SignedOutViewPreview(
    @PreviewParameter(SignedOutStateProvider::class) state: SignedOutState,
) = ElementPreview {
    SignedOutView(
        state = state,
    )
}
