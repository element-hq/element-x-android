/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.visuallist.VisualList
import io.element.android.libraries.designsystem.components.visuallist.VisualListItemData
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.accountinfo.AccountInfoCard
import io.element.android.libraries.matrix.ui.components.accountinfo.AccountInfoCardMode
import io.element.android.libraries.matrix.ui.components.accountinfo.AccountInfoCardState
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

/**
 * Ref: https://www.figma.com/design/kEAcfun9iSpszeUDvdKZ6b/ER-351--Multi-account-in-EX?node-id=795-6095
 */
@Composable
fun SignedOutView(
    state: SignedOutState,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = { state.eventSink(SignedOutEvent.Submit) })
    HeaderFooterPage(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        header = { SignedOutHeader(state) },
        content = { SignedOutContent(state.signedOutMatrixUser) },
        footer = {
            SignedOutFooter(
                onSubmit = { state.eventSink(SignedOutEvent.Submit) },
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
        iconStyle = BigIcon.Style.Default(
            CompoundIcons.UserProfileSolid(),
            usePrimaryTint = true,
        ),
    )
}

@Composable
private fun SignedOutContent(
    signedOutMatrixUser: MatrixUser,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AccountInfoCard(
            AccountInfoCardState(
                matrixUser = signedOutMatrixUser,
                mode = AccountInfoCardMode.Simple,
            )
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = BiasAlignment(
                horizontalBias = 0f,
                verticalBias = -0.4f
            )
        ) {
            VisualList(
                items = persistentListOf(
                    VisualListItemData(
                        message = stringResource(id = R.string.screen_signed_out_reason_1),
                        iconVector = CompoundIcons.Lock(),
                    ),
                    VisualListItemData(
                        message = stringResource(id = R.string.screen_signed_out_reason_2),
                        iconVector = CompoundIcons.Devices(),
                    ),
                    VisualListItemData(
                        message = stringResource(id = R.string.screen_signed_out_reason_3),
                        iconVector = CompoundIcons.Block(),
                    ),
                ),
            )
        }
    }
}

@Composable
private fun SignedOutFooter(
    onSubmit: () -> Unit,
) {
    ButtonColumnMolecule {
        Button(
            text = stringResource(id = CommonStrings.action_ok),
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SignedOutViewPreview(
    @PreviewParameter(SignedOutStatePreviewParam::class) state: SignedOutState,
) = ElementPreview {
    SignedOutView(
        state = state,
    )
}
