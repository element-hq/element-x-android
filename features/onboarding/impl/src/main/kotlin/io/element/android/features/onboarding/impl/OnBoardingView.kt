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

package io.element.android.features.onboarding.impl

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.element.android.libraries.designsystem.preview.ElementPreviews
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.pages.OnBoardingPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

// Refs:
// FTUE:
// - https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?type=design&node-id=133-5427&t=5SHVppfYzjvkEywR-0
// ElementX:
// - https://www.figma.com/file/0MMNu7cTOzLOlWb7ctTkv3/Element-X?type=design&node-id=1816-97419
@Composable
fun OnBoardingView(
    state: OnBoardingState,
    modifier: Modifier = Modifier,
    onSignInWithQrCode: () -> Unit = {},
    onSignIn: () -> Unit = {},
    onCreateAccount: () -> Unit = {},
) {
    OnBoardingPage(
        modifier = modifier,
        content = {
            OnBoardingContent()
        },
        footer = {
            OnBoardingButtons(
                state = state,
                onSignInWithQrCode = onSignInWithQrCode,
                onSignIn = onSignIn,
                onCreateAccount = onCreateAccount,
            )
        }
    )
}

@Composable
private fun OnBoardingContent(modifier: Modifier = Modifier) {
    // Note: having a night variant of R.drawable.onboarding_icon in the folder `drawable-night` is working
    // at runtime, but is not in Android Studio Preview. So I prefer to handle this manually.
    val isLight = ElementTheme.colors.isLight
    val iconDrawableRes = if (isLight) R.drawable.onboarding_icon_light else R.drawable.onboarding_icon_dark
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = BiasAlignment(
                horizontalBias = 0f,
                verticalBias = -0.4f
            )
        ) {
            // Dark and light icon does not have the same size, add padding to the smaller one
            val imagePadding = if (isLight) 28.dp else 0.dp
            Image(
                modifier = Modifier
                    .size(278.dp)
                    .padding(imagePadding),
                painter = painterResource(id = iconDrawableRes),
                contentDescription = null,
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = BiasAlignment(
                horizontalBias = 0f,
                verticalBias = 0.6f
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = R.string.screen_onboarding_welcome_title),
                    color = ElementTheme.materialColors.primary,
                    style = ElementTheme.typography.fontHeadingLgBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.screen_onboarding_welcome_message),
                    color = ElementTheme.materialColors.secondary,
                    style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = 17.sp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OnBoardingButtons(
    state: OnBoardingState,
    onSignInWithQrCode: () -> Unit,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ButtonColumnMolecule(modifier = modifier) {
        val signInButtonStringRes = if (state.canLoginWithQrCode || state.canCreateAccount) {
            R.string.screen_onboarding_sign_in_manually
        } else {
            CommonStrings.action_continue
        }
        if (state.canLoginWithQrCode) {
            Button(
                onClick = onSignInWithQrCode,
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = stringResource(id = R.string.screen_onboarding_sign_in_with_qr_code),
                    style = ElementTheme.typography.fontBodyLgMedium,
                )
            }
        }
        Button(
            onClick = onSignIn,
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.onBoardingSignIn)
        ) {
            Text(
                text = stringResource(id = signInButtonStringRes),
                style = ElementTheme.typography.fontBodyLgMedium,
            )
        }
        if (state.canCreateAccount) {
            OutlinedButton(
                onClick = onCreateAccount,
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.screen_onboarding_sign_up),
                    style = ElementTheme.typography.fontBodyLgMedium,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@ElementPreviews
@Composable
internal fun OnBoardingScreenLightPreview(@PreviewParameter(OnBoardingStateProvider::class) state: OnBoardingState) {
    ElementPreview { ContentToPreview(state) }
}

@Composable
private fun ContentToPreview(state: OnBoardingState) {
    OnBoardingView(state)
}
