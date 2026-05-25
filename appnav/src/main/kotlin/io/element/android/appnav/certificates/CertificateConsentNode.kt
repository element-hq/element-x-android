/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.certificates

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.appconfig.ApplicationConfig
import io.element.android.appnav.R
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.ui.strings.CommonStrings

@ContributesNode(AppScope::class)
@AssistedInject
class CertificateConsentNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : Node(
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun onConsentResult(accepted: Boolean)
    }

    private val callback: Callback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        val context = LocalContext.current
        BackHandler {
            callback.onConsentResult(false)
        }
        ConfirmationDialog(
            title = stringResource(id = R.string.screen_user_certificates_dialog_title),
            content = stringResource(id = R.string.screen_user_certificates_dialog_content, ApplicationConfig.PRODUCTION_APPLICATION_NAME),
            submitText = stringResource(id = R.string.screen_user_certificates_dialog_confirm),
            cancelText = stringResource(id = CommonStrings.action_no),
            onSubmitClick = {
                callback.onConsentResult(true)
            },
            onDismiss = {
                callback.onConsentResult(false)
            },
        )
    }
}
