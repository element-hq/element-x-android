/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.setup.biometric

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.lockscreen.impl.R
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
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
        state.eventSink(SetupBiometricEvents.UsePin)
    }
    HeaderFooterPage(
        modifier = modifier.padding(top = 80.dp),
        header = {
            SetupBiometricHeader()
        },
        footer = {
            SetupBiometricFooter(
                onAllowClick = { state.eventSink(SetupBiometricEvents.AllowBiometric) },
                onSkipClick = { state.eventSink(SetupBiometricEvents.UsePin) }
            )
        },
    )
}

@Composable
private fun SetupBiometricHeader() {
    val biometricAuth = stringResource(id = R.string.screen_app_lock_biometric_authentication)
    IconTitleSubtitleMolecule(
        iconStyle = BigIcon.Style.Default(Icons.Default.Fingerprint),
        title = stringResource(id = R.string.screen_app_lock_settings_enable_biometric_unlock),
        subTitle = stringResource(id = R.string.screen_app_lock_setup_biometric_unlock_subtitle, biometricAuth),
    )
}

@Composable
private fun SetupBiometricFooter(
    onAllowClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    ButtonColumnMolecule {
        val biometricAuth = stringResource(id = R.string.screen_app_lock_biometric_authentication)
        Button(
            text = stringResource(id = R.string.screen_app_lock_setup_biometric_unlock_allow_title, biometricAuth),
            onClick = onAllowClick
        )
        TextButton(
            text = stringResource(id = R.string.screen_app_lock_setup_biometric_unlock_skip),
            onClick = onSkipClick
        )
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
