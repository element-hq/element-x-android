/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.ftue.impl.welcome

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.ftue.impl.R
import io.element.android.libraries.designsystem.atomic.atoms.ElementLogoAtom
import io.element.android.libraries.designsystem.atomic.atoms.ElementLogoAtomSize
import io.element.android.libraries.designsystem.atomic.organisms.InfoListItem
import io.element.android.libraries.designsystem.atomic.organisms.InfoListOrganism
import io.element.android.libraries.designsystem.atomic.pages.OnBoardingPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WelcomeView(
    applicationName: String,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onContinueClick)
    OnBoardingPage(
        modifier = modifier
            .systemBarsPadding()
            .fillMaxSize(),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(78.dp))
                ElementLogoAtom(size = ElementLogoAtomSize.Medium)
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    modifier = Modifier.testTag(TestTags.welcomeScreenTitle),
                    text = stringResource(R.string.screen_welcome_title, applicationName),
                    style = ElementTheme.typography.fontHeadingMdBold,
                    color = ElementTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(80.dp))
                InfoListOrganism(
                    items = listItems(),
                    textStyle = ElementTheme.typography.fontBodyMdMedium,
                    iconTint = ElementTheme.colors.iconSecondary,
                    backgroundColor = ElementTheme.colors.bgCanvasDefault.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        },
        footer = {
            Button(
                text = stringResource(CommonStrings.action_continue),
                modifier = Modifier.fillMaxWidth(),
                onClick = onContinueClick
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    )
}

@Composable
private fun listItems() = persistentListOf(
    InfoListItem(
        message = stringResource(R.string.screen_welcome_bullet_2),
        iconVector = CompoundIcons.Lock(),
    ),
    InfoListItem(
        message = stringResource(R.string.screen_welcome_bullet_3),
        iconVector = CompoundIcons.ChatProblem(),
    ),
)

@PreviewsDayNight
@Composable
internal fun WelcomeViewPreview() {
    ElementPreview {
        WelcomeView(applicationName = "Element X", onContinueClick = {})
    }
}
