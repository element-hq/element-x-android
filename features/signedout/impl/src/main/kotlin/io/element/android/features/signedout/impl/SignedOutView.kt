/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.organisms.InfoListItem
import io.element.android.libraries.designsystem.atomic.organisms.InfoListOrganism
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.temporaryColorBgSpecial
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
        iconStyle = BigIcon.Style.Default(Icons.Filled.AccountCircle),
    )
}

@Composable
private fun SignedOutContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BiasAlignment(
            horizontalBias = 0f,
            verticalBias = -0.4f
        )
    ) {
        InfoListOrganism(
            items = persistentListOf(
                InfoListItem(
                    message = stringResource(id = R.string.screen_signed_out_reason_1),
                    iconVector = CompoundIcons.Lock(),
                ),
                InfoListItem(
                    message = stringResource(id = R.string.screen_signed_out_reason_2),
                    iconVector = CompoundIcons.Devices(),
                ),
                InfoListItem(
                    message = stringResource(id = R.string.screen_signed_out_reason_3),
                    iconVector = CompoundIcons.Block(),
                ),
            ),
            textStyle = ElementTheme.typography.fontBodyMdMedium,
            iconTint = ElementTheme.colors.iconSecondary,
            backgroundColor = ElementTheme.colors.temporaryColorBgSpecial
        )
    }
}

@Composable
private fun SignedOutFooter(
    onSignInAgain: () -> Unit,
) {
    ButtonColumnMolecule {
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
