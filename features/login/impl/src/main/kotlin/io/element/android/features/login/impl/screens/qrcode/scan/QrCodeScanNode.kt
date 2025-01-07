/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.login.impl.di.QrCodeLoginScope
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData

@ContributesNode(QrCodeLoginScope::class)
class QrCodeScanNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: QrCodeScanPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onScannedCode(qrCodeLoginData: MatrixQrCodeLoginData)
        fun onCancelClicked()
    }

    private fun onQrCodeDataReady(qrCodeLoginData: MatrixQrCodeLoginData) {
        plugins<Callback>().forEach { it.onScannedCode(qrCodeLoginData) }
    }

    private fun onCancelClicked() {
        plugins<Callback>().forEach { it.onCancelClicked() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        QrCodeScanView(
            state = state,
            onQrCodeDataReady = ::onQrCodeDataReady,
            onBackClick = ::onCancelClicked,
            modifier = modifier
        )
    }
}
