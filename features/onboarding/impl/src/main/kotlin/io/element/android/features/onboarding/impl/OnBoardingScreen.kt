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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag

@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    onSignInWithQrCode: () -> Unit = {},
    onSignIn: () -> Unit = {},
    onCreateAccount: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // BG
        Image(
            modifier = modifier
                .fillMaxSize(),
            painter = painterResource(id = R.drawable.onboarding_bg),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.weight(2f))
                OnBoardingHeader()
                Spacer(modifier = Modifier.weight(3f))
            }
            OnBoardingButtons(
                onSignInWithQrCode = onSignInWithQrCode,
                onSignIn = onSignIn,
                onCreateAccount = onCreateAccount,
            )
        }
    }
}

@Composable
private fun ColumnScope.OnBoardingHeader() {
    Column(
        modifier = Modifier
            .weight(3f)
            .fillMaxWidth(),
        horizontalAlignment = CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.element_logo),
            contentDescription = null,
        )
        Image(
            modifier = Modifier.padding(top = 14.dp),
            painter = painterResource(id = R.drawable.element),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentDescription = null,
        )
        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = stringResource(id = R.string.screen_onboarding_subtitle),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OnBoardingButtons(
    onSignInWithQrCode: () -> Unit,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = {
                onSignInWithQrCode()
            },
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.QrCode, contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.width(14.dp))
            Text(text = stringResource(id = R.string.screen_onboarding_sign_in_with_qr_code))
        }
        Button(
            onClick = {
                onSignIn()
            },
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.onBoardingSignIn)
        ) {
            Text(text = stringResource(id = R.string.screen_onboarding_sign_in_manually))
        }
        TextButton(
            onClick = {
                onCreateAccount()
            },
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.screen_onboarding_sign_up))
        }
    }
}

@Preview
@Composable
internal fun OnBoardingScreenLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun OnBoardingScreenDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    OnBoardingScreen()
}
