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

package io.element.android.features.signedout.impl

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

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
        header = { SignedOutHeader(state) },
        content = { SignedOutContent() },
        footer = {
            SignedOutFooter(
                onSignInAgain = { state.eventSink(SignedOutEvents.SignInAgain) },
            )
        }
    )
}

@Composable
private fun SignedOutHeader(state: SignedOutState) {
    IconTitleSubtitleMolecule(
        modifier = Modifier.padding(top = 60.dp, bottom = 12.dp),
        title = stringResource(id = R.string.screen_signed_out_title),
        subTitle = stringResource(id = R.string.screen_signed_out_subtitle, state.appName),
        iconImageVector = Icons.Filled.AccountCircle,
        iconTint = ElementTheme.colors.iconSecondary,
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
                    message = stringResource(id = R.string.screen_signed_out_reason_1),
                    iconComposable = { Icon(R.drawable.ic_lock_outline) },
                ),
                InfoListItem(
                    message = stringResource(id = R.string.screen_signed_out_reason_2),
                    iconComposable = { Icon(R.drawable.ic_devices) },
                ),
                InfoListItem(
                    message = stringResource(id = R.string.screen_signed_out_reason_3),
                    iconComposable = { Icon(R.drawable.ic_do_disturb_alt) },
                ),
            ),
            textStyle = ElementTheme.typography.fontBodyMdMedium,
            iconTint = ElementTheme.colors.textPrimary,
            backgroundColor = ElementTheme.colors.temporaryColorBgSpecial
        )
    }
}

@Composable
private fun Icon(
    @DrawableRes iconResourceId: Int,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier
            .size(20.dp),
        resourceId = iconResourceId,
        contentDescription = null,
        tint = ElementTheme.colors.iconSecondary,
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
            text = stringResource(id = CommonStrings.action_sign_in_again),
            onClick = onSignInAgain,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SignedOutViewPreview(
    @PreviewParameter(SignedOutStateProvider::class) state: SignedOutState,
) = ElementPreview {
    SignedOutView(
        state = state,
    )
}
