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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.features.ftue.impl.R
import io.element.android.libraries.designsystem.atomic.atoms.ElementLogoAtom
import io.element.android.libraries.designsystem.atomic.atoms.ElementLogoAtomSize
import io.element.android.libraries.designsystem.atomic.molecules.InfoListItem
import io.element.android.libraries.designsystem.atomic.molecules.InfoListOrganism
import io.element.android.libraries.designsystem.atomic.pages.OnBoardingPage
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WelcomeView(
    applicationName: String,
    modifier: Modifier = Modifier,
    onContinueClicked: () -> Unit,
) {
    BackHandler(onBack = onContinueClicked)
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.screen_welcome_subtitle),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(40.dp))
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
                onClick = onContinueClicked
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    )
}

@Composable
private fun listItems() = persistentListOf(
    InfoListItem(
        message = stringResource(R.string.screen_welcome_bullet_1),
        iconVector = Icons.Outlined.NewReleases,
    ),
    InfoListItem(
        message = stringResource(R.string.screen_welcome_bullet_2),
        iconVector = Icons.Outlined.Lock,
    ),
    InfoListItem(
        message = stringResource(R.string.screen_welcome_bullet_3),
        iconVector = Icons.Outlined.AddComment,
    ),
)

@DayNightPreviews
@Composable
internal fun WelcomeViewPreview() {
    ElementPreview {
        WelcomeView(applicationName = "Element X", onContinueClicked = {})
    }
}
